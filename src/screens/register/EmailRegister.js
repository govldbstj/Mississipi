import React, { useState, useEffect } from 'react';
import styled from 'styled-components/native';
import FormTextInput from '../../components/molecules/FormTextInput';
import Button from '../../components/atoms/Button';
import { emailSignUp } from '../../controllers/LoginController';

const Container = styled.View`
    flex: 1;
    justify-content: center;
    align-items: center;
`;

const EmailRegister = ({ navigation }) => {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [passwordRemind, setPasswordRemind] = useState('');

    const SetandSent = async () => {
        console.log('EmailRegister:', name, email, password);

        const result = await emailSignUp(name, email, password, passwordRemind);

        if (result.isSuccess()) {
            alert('가입에 성공하였습니다.');
            navigation.pop();
        } else {
            if (result.isInAppFailure()) {
                alert(result.tryGetErrorMessage());
                return;
            }
            alert('가입에 실패하였습니다. 다시 시도해주세요.');
            console.log('EmailRegisterError', result.tryGetErrorCode(), result.tryGetErrorMessage());
        }
    };

    return (
        <Container>
            <FormTextInput
                label="이름"
                placeholder="이름을 입력하세요."
                onTextChangeListener={(text) => {
                    setName(text);
                }}
            />
            <FormTextInput
                label="이메일"
                placeholder="이메일을 입력하세요."
                onTextChangeListener={(text) => {
                    setEmail(text);
                }}
            />
            <FormTextInput
                label="비밀번호"
                placeholder="비밀번호를 8자 이상 입력하세요."
                isPassword={true}
                onTextChangeListener={(text) => {
                    setPassword(text);
                }}
            />
            <FormTextInput
                label="비밀번호 확인"
                placeholder="비밀번호를 똑같이 입력하세요."
                isPassword={true}
                onTextChangeListener={(text) => {
                    setPasswordRemind(text);
                }}
            />
            <Button title="회원 가입" onPress={() => SetandSent()} />
        </Container>
    );
};

export default EmailRegister;
