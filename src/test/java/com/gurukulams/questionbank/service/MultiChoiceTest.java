package com.gurukulams.questionbank.service;

import com.gurukulams.questionbank.model.QuestionChoice;
import com.gurukulams.questionbank.payload.Question;
import com.gurukulams.questionbank.payload.QuestionType;

import java.util.stream.Collectors;

public class MultiChoiceTest extends ChoseTheBestTest {
    @Override
    String getCorrectAnswer(Question question) {
        return question.getChoices().stream()
                .filter(QuestionChoice::isAnswer)
                .map(choice -> choice.id().toString())
                .collect(Collectors.joining(","));
    }

    @Override
    Question getTestQuestion() {
        Question question = super.getTestQuestion();
        question.setType(QuestionType.MULTI_CHOICE);

        question.setQuestion("Which of the following are programing Languages?");

        for (int i = 0; i < question.getChoices().size(); i++) {
            if(question.getChoices().get(i).cValue().equals(C_LANGUAGE)) {
                question.getChoices().set(i,question.getChoices().get(i).withIsAnswer(true))  ;
            }
        }



        return question;
    }
}
