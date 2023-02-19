import React, { useState } from 'react';
import styled from 'styled-components/native';
import FormTextInput from '../../components/molecules/FormTextInput';
import Button from '../../components/atoms/Button';
import * as Api from '../../controllers/ApiController';

const Container = styled.View`
    flex: 1;
    justify-content: center;
    align-items: center;
`;

const Email = ({ navigation }) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const SetandSent = async () => {
        console.log('Email:', email, password);

        if (email.length === 0) {
            alert('이메일을 입력해주세요.');
            return;
        }
        if (password.length < 8) {
            alert('비밀번호를 8자 이상 입력해주세요.');
            return;
        }

        const result = await Api.post('login', {
            body: {
                email: email,
                password: password,
            },
        });

        if (result.isSuccess()) {
            alert('로그인에 성공하였습니다.');
            navigation.popToTop();
        } else {
            alert('로그인에 실패하였습니다. 다시 시도해주세요.');
            console.log('EmailError', result.tryGetErrorCode(), result.tryGetErrorMessage());
        }
    };

    return (
        <Container>
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
            <Button title="로그인" onPress={() => SetandSent()} />
        </Container>
    );
};

export default Email;
