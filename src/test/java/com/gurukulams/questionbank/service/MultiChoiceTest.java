package com.gurukulams.questionbank.service;

import com.gurukulams.questionbank.model.QuestionChoice;
import com.gurukulams.questionbank.payload.Question;
import com.gurukulams.questionbank.payload.QuestionType;

import java.util.stream.Collectors;

public class MultiChoiceTest extends ChoseTheBestTest {
    @Override
    String getCorrectAnswer(Question question) {
        return question.getChoices().stream()
                .filter(QuestionChoice::getIsAnswer)
                .map(choice -> choice.getId().toString())
                .collect(Collectors.joining(","));
    }

    @Override
    Question getTestQuestion() {
        Question question = super.getTestQuestion();
        question.setType(QuestionType.MULTI_CHOICE);

        question.setQuestion("Which of the following are programing Languages?");

        question.getChoices().stream()
                .filter(questionChoice ->
                        questionChoice.getCValue().equals(C_LANGUAGE))
                .findFirst()
                .get().setIsAnswer(true);

        return question;
    }
}
