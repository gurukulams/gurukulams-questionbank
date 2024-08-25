package com.gurukulams.questionbank.service;

import com.gurukulams.questionbank.model.QuestionChoice;
import com.gurukulams.questionbank.payload.Question;
import com.gurukulams.questionbank.payload.QuestionType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class MatchTheFollowingTest extends ChoseTheBestTest {

    @Override
    String getCorrectAnswer(Question question) {
        List<QuestionChoice> questionChoices = new ArrayList<>();

        questionChoices
                .addAll(question.getChoices());

        questionChoices
                .addAll(question.getMatches().subList(0, question.getChoices().size()));

        return questionChoices.stream()
                .map(choice -> choice.getId().toString())
                .collect(Collectors.joining(","));
    }

    @Override
    List<Question> getInvalidQuestions() {

        List<Question> invalidQuestions = new ArrayList<>();

        Question question = getTestQuestion();
        //null matches
        question.setMatches(null);
        invalidQuestions.add(question);
        return invalidQuestions;
    }

    @Override
    Question getTestQuestion() {
        Question question = super.getTestQuestion();
        question.setType(QuestionType.MATCH_THE_FOLLOWING);

        question.setQuestion("Match the Following");

        question.getChoices()
                .forEach(questionChoice ->
                        questionChoice.setIsAnswer(false));

        question.setMatches(getMatches());

        return question;
    }

    private List<QuestionChoice> getMatches() {
        List<QuestionChoice> matches = new ArrayList<>();

        QuestionChoice choice = new QuestionChoice();
        choice.setCValue("Object Oriented");
        matches.add(choice);

        choice = new QuestionChoice();
        choice.setCValue("System Language");
        matches.add(choice);

        choice = new QuestionChoice();
        choice.setCValue("Regional Language");
        matches.add(choice);

        choice = new QuestionChoice();
        choice.setCValue("Universal Language");
        matches.add(choice);

        return matches;
    }


}