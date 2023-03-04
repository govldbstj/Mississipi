package com.backend.waste.service;


import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.waste.domain.Waste;
import com.backend.waste.dto.request.PatchWaste;
import com.backend.waste.dto.response.GetWasteBrief;
import com.backend.waste.dto.response.GetWasteDetail;
import com.backend.waste.repository.WasteRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class WasteService {

    private final MemberRepository memberRepository;
    private final WasteRepository wasteRepository;


    @Value("${spring.file.path}")
    private String uploadFolder;

    @Transactional
    public Waste createWaste(Long memberIdx, MultipartFile image) throws IOException {
        Member member = memberRepository.getById(memberIdx);

        UUID uuid = UUID.randomUUID();
        String imageFileName = uuid + "_" + image.getOriginalFilename();
        Path imageFilePath = Paths.get(uploadFolder + "/" + imageFileName);
        Files.write(imageFilePath, image.getBytes());
        Waste waste = Waste.createWaste(member, imageFileName);

        PatchWaste patchWaste = requestToML(image);
        waste.update(patchWaste.getWasteName(), patchWaste.getPrice());
        wasteRepository.save(waste);
        return waste;
    }

    @Transactional
    public List<GetWasteBrief> getWasteList(Long id) {
        Member member = memberRepository.getById(id);
        List<Waste> wastes = wasteRepository.getWasteList(member);

        List<GetWasteBrief> result = new ArrayList<>();
        for (Waste waste : wastes) {
            result.add(new GetWasteBrief(waste.getId(), waste.getName(), waste.getEnrolledDate(), getTimeGap(waste)));
        }
        return result;
    }

    String getTimeGap(Waste waste) {
        StringBuilder sb = new StringBuilder();
        if (waste.isCollected())
            sb.append("수거 완료");
        else if (waste.getCollector() == null)
            sb.append("등록 완료");
        else {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime time = waste.getCollectedDate();
            if (ChronoUnit.YEARS.between(now, time) != 0) {
                long yearGap = ChronoUnit.YEARS.between(now, time);
                sb.append(yearGap).append("년 ");
                time = time.minusYears(yearGap);
            }
            if (ChronoUnit.MONTHS.between(now, time) != 0) {
                long monthGap = ChronoUnit.MONTHS.between(now, time);
                sb.append(monthGap).append("달 ");
                time = time.minusMonths(monthGap);
            }
            if (ChronoUnit.DAYS.between(now, time) != 0) {
                long dayGap = ChronoUnit.DAYS.between(now, time);
                sb.append(dayGap).append("일 ");
                time = time.minusDays(dayGap);
            }
            if (ChronoUnit.HOURS.between(now, time) != 0) {
                long hourGap = ChronoUnit.HOURS.between(now, time);
                sb.append(hourGap).append("시간 ");
                time = time.minusHours(hourGap);
            }
            sb.append(ChronoUnit.MINUTES.between(now, time)).append("분 후 수거");
        }

        return sb.toString();
    }

    public GetWasteDetail getWaste(Long userIdx, Long wasteIdx) throws IOException {
        Member member = memberRepository.getById(userIdx);
        Waste waste = wasteRepository.getById(wasteIdx);

        byte[] imageByteArray = null;
        InputStream imageStream = new FileInputStream((uploadFolder + "/" + waste.getImage()));
        imageByteArray = imageStream.readAllBytes();
        imageStream.close();

        String imageName = waste.getImage();
        String fileExtension = imageName.substring(imageName.lastIndexOf(".") + 1).toUpperCase(Locale.ROOT);

        return new GetWasteDetail(
                member.getNickname(),
                member.getAddress(),
                waste.getName(),
                waste.getPrice(),
                getTimeGap(waste),
                fileExtension,
                imageByteArray
        );
    }

    private String getBase64String(MultipartFile image) throws IOException {
        byte[] bytes = image.getBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Transactional
    public PatchWaste requestToML(MultipartFile image) throws IOException {

        String url = "http://localhost:8080/waste-management/ML";

        RestTemplate restTemplate = new RestTemplate();

        //인코딩용
        HttpHeaders header1 = new HttpHeaders();
        header1.setContentType(MediaType.APPLICATION_JSON);
        MultiValueMap<String, Object> bodyStr = new LinkedMultiValueMap<>();
        String imageFileString = getBase64String(image);
        String fileExtension = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf(".") + 1);
        bodyStr.add("imageType", fileExtension.toUpperCase(Locale.ROOT));
        bodyStr.add("value", imageFileString);

        System.out.println(bodyStr);

        //파일용
//        HttpHeaders header2 = new HttpHeaders();
//        header2.setContentType(MediaType.MULTIPART_FORM_DATA);
//        MultiValueMap<String, Object> bodyFile = new LinkedMultiValueMap<>();
//        bodyFile.add("image", new ByteArrayResource(image.getBytes()));

        HttpEntity<?> requestMessage = new HttpEntity<>(bodyStr, header1);
        HttpEntity<String> response = restTemplate.postForEntity(url, requestMessage, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        PatchWaste patchWaste = objectMapper.readValue(response.getBody(), PatchWaste.class);
        return patchWaste;
    }

}
