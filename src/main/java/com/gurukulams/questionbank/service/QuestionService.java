package com.gurukulams.questionbank.service;

import com.gurukulams.questionbank.QuestionBankManager;
import com.gurukulams.questionbank.model.Matches;
import com.gurukulams.questionbank.model.QuestionCategory;
import com.gurukulams.questionbank.model.QuestionChoice;
import com.gurukulams.questionbank.model.QuestionChoiceLocalized;
import com.gurukulams.questionbank.model.QuestionLocalized;
import com.gurukulams.questionbank.payload.Question;
import com.gurukulams.questionbank.payload.QuestionType;

import com.gurukulams.questionbank.store.MatchesStore;
import com.gurukulams.questionbank.store.QuestionCategoryStore;
import com.gurukulams.questionbank.store.QuestionChoiceLocalizedStore;
import com.gurukulams.questionbank.store.QuestionChoiceStore;
import com.gurukulams.questionbank.store.QuestionLocalizedStore;
import com.gurukulams.questionbank.store.QuestionStore;
import com.gurukulams.questionbank.store.QuestionTagStore;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;

import java.lang.annotation.ElementType;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.gurukulams.questionbank.store.MatchesStore.questionId;

/**
 * The type Question service.
 */
public class QuestionService {

    /**
     * Owner of QB.
     */
    public static final String OWNER_USER = "tom";

    /**
     * Validator.
     */
    private final Validator validator;

    /**
     * QuestionStore.
     */
    private final QuestionStore questionStore;

    /**
     * QuestionLocalized.
     */
    private final QuestionLocalizedStore questionLocalizedStore;

    /**
     * QuestionChoiceStore.
     */
    private final QuestionChoiceStore questionChoiceStore;

    /**
     * Store for Matches.
     */
    private final MatchesStore matchesStore;

    /**
     * QuestionChoiceLocalized.
     */
    private final QuestionChoiceLocalizedStore questionChoiceLocalizedStore;


    /**
     * QuestionCategoryStore.
     */
    private final QuestionCategoryStore questionCategoryStore;


    /**
     * QuestionTagStore.
     */
    private final QuestionTagStore questionTagStore;


    /**
     * initializes.
     *
     * @param aValidator        thevalidator
     * @param gurukulamsManager
     */
    public QuestionService(final Validator aValidator,
                           final QuestionBankManager gurukulamsManager) {
        this.validator = aValidator;
        this.questionStore = gurukulamsManager
                .getQuestionStore();
        this.questionLocalizedStore = gurukulamsManager
                .getQuestionLocalizedStore();
        this.questionChoiceStore = gurukulamsManager
                .getQuestionChoiceStore();
        this.questionChoiceLocalizedStore = gurukulamsManager
                .getQuestionChoiceLocalizedStore();
        this.questionCategoryStore = gurukulamsManager
                .getQuestionCategoryStore();
        this.questionTagStore = gurukulamsManager
                .getQuestionTagStore();
        this.matchesStore = gurukulamsManager.getMatchesStore();
    }

    /**
     * inserts data.
     *
     * @param categories the category
     * @param type       the type
     * @param tag
     * @param locale     the locale
     * @param createdBy  the createdBy
     * @param question   the question
     * @return question optional
     */
    public Optional<Question> create(
            final List<String> categories,
            final List<String> tag,
            final QuestionType type,
            final Locale locale,
            final String createdBy,
            final Question question) throws SQLException {
        question.setType(type);
        Set<ConstraintViolation<Question>> violations =
                getViolations(question);
        if (violations.isEmpty()) {
            final UUID id = UUID.randomUUID();

            question.setId(id);
            question.setType(type);
            question.setCreatedAt(LocalDateTime.now());

            this.questionStore
                    .insert()
                    .values(getQuestionModel(createdBy, question))
                    .execute();
            if (locale != null) {

                createLocalized(locale, question, id);
            }
            List<QuestionChoice> choices = null;
            if ((question.getType().equals(QuestionType.CHOOSE_THE_BEST)
                || question.getType().equals(QuestionType.MULTI_CHOICE)
                || question.getType().equals(QuestionType.MATCH_THE_FOLLOWING)
                )) {
                choices = createChoices(question.getChoices(), locale, id);
            }

            if (question.getType().equals(QuestionType.MATCH_THE_FOLLOWING)) {
                List<QuestionChoice> matches
                        = createChoices(question.getMatches(), locale, id);

                List<Matches> matchesToCreate = new ArrayList<>();

                int choiceSize = choices.size();

                List<QuestionChoice> extraMatch
                        = matches.stream().skip(choiceSize).toList();

                for (int i = 0; i < choiceSize; i++) {
                    QuestionChoice choice = choices.get(i);
                    QuestionChoice matchChoice = matches.get(i);

                    UUID choiceId = choice.getId();
                    UUID matchId = matchChoice.getId();

                    Matches match = new Matches();
                    match.setQuestionId(id);
                    match.setChoiceId(choiceId);
                    match.setMatchId(matchId);

                    matchesToCreate.add(match);
                }

                if (!extraMatch.isEmpty()) {
                    for (QuestionChoice eMatch : extraMatch) {

                        Matches match = new Matches();
                        UUID extraMatchId = eMatch.getId();
                        match.setQuestionId(id);
                        match.setMatchId(extraMatchId);

                        matchesToCreate.add(match);
                    }
                }

                List<Matches> createdMatches = createMatches(matchesToCreate);
            }


            for (String category : categories) {
                attachCategory(createdBy,
                        id, category);
            }

            return read(id, locale);
        } else {
            throw new ConstraintViolationException(violations);
        }

    }



    private int createLocalized(final Locale locale,
                                final Question question,
                                final UUID id)
            throws SQLException {
        QuestionLocalized questionLocalized = new QuestionLocalized();

        questionLocalized.setQuestionId(id);
        questionLocalized.setQuestion(question.getQuestion());
        questionLocalized.setExplanation(question.getExplanation());
        questionLocalized.setLocale(locale.getLanguage());

        return this.questionLocalizedStore
                .insert()
                .values(questionLocalized)
                .execute();
    }

    private com.gurukulams.questionbank.model.Question
    getQuestionModel(final String createdBy, final Question question) {
        com.gurukulams.questionbank.model.Question questionModel
                = new com.gurukulams.questionbank.model.Question();
        questionModel.setQuestion(question.getQuestion());
        questionModel.setExplanation(question.getExplanation());
        questionModel.setId(question.getId());
        questionModel.setAnswer(question.getAnswer());
        questionModel.setType(question.getType().name());
        questionModel.setCreatedBy(createdBy);
        questionModel.setCreatedAt(question.getCreatedAt());
        return questionModel;
    }

    private Question
    getQuestion(final com.gurukulams.questionbank.model.Question
                        questionModel) {
        Question question
                = new Question();
        question.setQuestion(questionModel.getQuestion());
        question.setExplanation(questionModel.getExplanation());
        question.setId(questionModel.getId());
        question.setAnswer(questionModel.getAnswer());
        question.setType(QuestionType.valueOf(questionModel.getType()));
        question.setCreatedBy(questionModel.getCreatedBy());
        question.setCreatedAt(questionModel.getCreatedAt());
        question.setUpdatedAt(questionModel.getModifiedAt());
        return question;
    }

    private QuestionChoice createChoice(
            final QuestionChoice choice,
            final Locale locale,
            final UUID questionId) throws SQLException {

        if (choice == null) {
            return null;
        }
        UUID choiceId = UUID.randomUUID();

        choice.setId(choiceId);
        choice.setQuestionId(questionId);
        if (choice.getIsAnswer() == null) {
            choice.setIsAnswer(Boolean.FALSE);
        }
        this.questionChoiceStore.insert().values(choice)
                .execute();

        if (locale != null) {
            choice.setId(choiceId);
            createLocalizedChoice(locale, choice);
        }

        return choice;
    }

    private Matches createMatch(
            final UUID questionId,
            final UUID choiceId,
            final UUID matchId
    ) throws SQLException {

        Matches matches = new Matches();

        matches.setQuestionId(questionId);
        matches.setChoiceId(choiceId);
        matches.setMatchId(matchId);
        this.matchesStore.insert().values(matches)
                .execute();

        return matches;
    }


    private void createLocalizedChoice(final Locale locale,
                                       final QuestionChoice choice)
            throws SQLException {
        QuestionChoiceLocalized questionChoiceLocalized
                = new QuestionChoiceLocalized();

        questionChoiceLocalized.setChoiceId(choice.getId());
        questionChoiceLocalized.setLocale(locale.getLanguage());
        questionChoiceLocalized.setCValue(choice.getCValue());

        this.questionChoiceLocalizedStore
                .insert()
                .values(questionChoiceLocalized).execute();
    }

    private void saveLocalizedChoice(final Locale locale,
                                     final QuestionChoice choice)
            throws SQLException {

        int updatedRows = this.questionChoiceLocalizedStore
                .update()
                .set(QuestionChoiceLocalizedStore
                        .cValue(choice.getCValue()))
                .where(QuestionChoiceLocalizedStore
                        .choiceId().eq(choice.getId())
                        .and(QuestionChoiceLocalizedStore
                                .locale().eq(locale.getLanguage())))
                .execute();
        if (updatedRows == 0) {
            createLocalizedChoice(locale, choice);
        }
    }

    private List<QuestionChoice> createChoices(
            final List<QuestionChoice> choices,
            final Locale locale,
            final UUID id) throws SQLException {
        List<QuestionChoice> createdChoices = new ArrayList<>();
        if (choices != null) {
            for (QuestionChoice choice : choices) {
                createdChoices.add(createChoice(choice, locale, id));
            }
        }
        return createdChoices;
    }
    private List<Matches> createMatches(
            final List<Matches> matches) throws SQLException {
        List<Matches> createdMatches = new ArrayList<>();


        if (matches != null) {
            for (Matches match : matches) {
                createdMatches.add(createMatch(match.getQuestionId(),
                        match.getChoiceId(), match.getMatchId()));
            }
        }
        return createdMatches;
    }



    /**
     * List question choice list.
     *
     * @param isOwner    isOwner calling
     * @param questionId the question choice id
     * @param locale
     * @return the list
     */
    private List<QuestionChoice> listChoices(final boolean isOwner,
                                             final UUID questionId,
                                             final Locale locale)
            throws SQLException {
        if (locale == null) {
            List<QuestionChoice> choices = this.questionChoiceStore
                    .select(QuestionChoiceStore.questionId().eq(questionId))
                    .execute();

            if (!isOwner) {
                choices.forEach(choice
                        -> choice.setIsAnswer(null));
            }
            return choices;
        } else {
            final String query =  "SELECT id,question_id,"
                    + "CASE WHEN qcl.LOCALE = ? "
                    + "THEN qcl.c_value "
                    + "ELSE qc.c_value "
                    + "END AS c_value, "
                    + (isOwner ? "is_answer" : "NULL")
                    + " AS is_answer"
                    + " FROM question_choice qc "
                    + "LEFT JOIN question_choice_localized qcl ON"
                    + " qc.ID = qcl.choice_id WHERE"
                    + " question_id = ? AND ( qcl.LOCALE IS NULL OR "
                    + "qcl.LOCALE = ? OR qc.ID "
                    + "NOT IN (SELECT choice_id FROM "
                    + "question_choice_localized WHERE "
                    + "choice_id=qc.ID AND LOCALE = ?))";

            return this.questionChoiceStore
                    .select()
                    .sql(query)
                    .param(QuestionChoiceLocalizedStore
                            .locale(locale.getLanguage()))
                    .param(QuestionChoiceStore
                            .questionId(questionId))
                    .param(QuestionChoiceLocalizedStore
                            .locale(locale.getLanguage()))
                    .param(QuestionChoiceLocalizedStore
                            .locale(locale.getLanguage()))
                    .list();

        }
    }

    /**
     * reads from question with given id.
     *
     * @param id     the id
     * @param locale
     * @return question optional
     */
    public Optional<Question> read(final UUID id,
                                   final Locale locale) throws SQLException {

        Optional<com.gurukulams.questionbank.model.Question> qm;

        if (locale == null) {
            qm = this.questionStore.select(id);
        } else {
            final String query = """
                SELECT id,
                       CASE WHEN ql.LOCALE = ?
                       THEN ql.question ELSE q.question END AS question,
                       CASE WHEN ql.LOCALE = ?
                       THEN ql.explanation ELSE q.explanation
                       END AS explanation,
                       type, answer, created_at,created_by,
                       modified_at,modified_by
                FROM question q
                LEFT JOIN question_localized ql ON q.ID = ql.QUESTION_ID
                WHERE q.id = ?
                AND (ql.LOCALE IS NULL OR ql.LOCALE = ? OR q.ID NOT IN (
                    SELECT question_id
                    FROM question_localized
                    WHERE QUESTION_ID = q.ID AND LOCALE = ?
                ))
                """;

            qm = this.questionStore.select()
                    .sql(query)
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()))
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()))
                    .param(QuestionStore.id(id))
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()))
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()))
                    .optional();

        }

        if (qm.isPresent()) {
            Optional<Question> question = qm.map(this::getQuestion);
            fillChoices(locale, question.get());
            return question;
        }

        return Optional.empty();
    }

    private void fillChoices(final Locale locale,
                             final Question question)
            throws SQLException {
        if (question.getType()
                .equals(QuestionType.CHOOSE_THE_BEST)
                || question.getType()
                .equals(QuestionType.MULTI_CHOICE)) {
            question.setChoices(
                    listChoices(true,
                            question.getId(), locale));
        } else if (question.getType()
                .equals(QuestionType.MATCH_THE_FOLLOWING)) {
            // All Choices are available
            List<QuestionChoice> allChoices = listChoices(true,
                    question.getId(), locale);
            // Match Pairs are available
            List<Matches>  matchePairs =
                    this.matchesStore.select(questionId().eq(question
                            .getId())).execute();


            List<QuestionChoice> choices = new ArrayList<>();
            List<QuestionChoice> matches = new ArrayList<>();

            matchePairs.stream().filter(matchPair ->
                    matchPair.getChoiceId() != null).forEach(matchPair -> {
                choices.add(allChoices.stream()
                        .filter(chice -> chice.getId()
                                .equals(matchPair.getChoiceId()))
                        .findFirst()
                        .get());

                matches.add(allChoices.stream()
                        .filter(chice -> chice.getId()
                                .equals(matchPair.getMatchId()))
                        .findFirst().get());


            });

            matchePairs.stream().filter(matchPair
                    -> matchPair.getChoiceId() == null)
                    .forEach(matchPair -> {
                matches.add(allChoices.stream()
                        .filter(chice -> chice.getId()
                                .equals(matchPair.getMatchId()))
                        .findFirst().get());
            });

            question.setChoices(choices);
            question.setMatches(matches);




        }
    }

    /**
     * updates question with id.
     *
     * @param id       the id
     * @param locale   the language
     * @param type     the type
     * @param question the question
     * @return question optional
     */
    public Optional<Question> update(
            final QuestionType type,
            final UUID id,
            final Locale locale,
            final Question question) throws SQLException {
        question.setType(type);
        Set<ConstraintViolation<Question>> violations =
                getViolations(question);
        if (violations.isEmpty()) {

            int updatedRows = 0;
            if (locale == null) {
                updatedRows = this.questionStore
                        .update()
                        .set(QuestionStore.question(question.getQuestion()),
                        QuestionStore.explanation(question.getExplanation()),
                        QuestionStore.answer(question.getAnswer()),
                        QuestionStore.modifiedAt(LocalDateTime.now()))
                        .where(QuestionStore.id().eq(id)
                                .and().type().eq(type.toString()))
                        .execute();
            } else {
                updatedRows = this.questionStore
                        .update()
                        .set(QuestionStore.answer(question.getAnswer()),
                                QuestionStore.modifiedAt(LocalDateTime.now()))
                        .where(QuestionStore.id().eq(id)
                                .and().type().eq(type.toString()))
                        .execute();
            }

            if (locale != null) {


                final String localizedUpdateQuery = """
                        UPDATE QUESTION_LOCALIZED SET question = ?,
                        explanation = ?
                            WHERE question_id = ? AND
                                    locale = ? AND
                                question_id IN
                                    ( SELECT id from question
                                            where type
                                            = ?  )
                        """;

                updatedRows = this.questionLocalizedStore
                    .update()
                    .sql(localizedUpdateQuery)
                    .param(QuestionLocalizedStore
                            .question(question.getQuestion()))
                    .param(QuestionLocalizedStore
                            .explanation(question.getExplanation()))
                    .param(QuestionLocalizedStore
                            .questionId(id))
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()))
                    .param(QuestionStore.type(type.toString()))
                    .execute();

                if (updatedRows == 0) {

                    updatedRows = createLocalized(locale, question, id);

                }
            }

            if ((type.equals(QuestionType.CHOOSE_THE_BEST)
                    || type.equals(QuestionType.MULTI_CHOICE)
                    || QuestionType.MATCH_THE_FOLLOWING.equals(type))
                    && question.getChoices() != null) {

                List<UUID> availableIds = question.getChoices()
                        .stream()
                        .filter(choice -> choice.getId() != null)
                        .map(QuestionChoice::getId)
                        .collect(Collectors.toList());

                if (!availableIds.isEmpty()) {

                    final String deleteLocallizedChoiceSQL =
                    "DELETE FROM question_choice_localized WHERE"
                            + " choice_id IN (SELECT id FROM question_choice "
                            + "WHERE question_id = ? AND id NOT IN ("
                            + availableIds.stream()
                            .map(aId -> "?")
                            .collect(Collectors.joining(","))
                            + "))";
                    availableIds.add(0, id);

                    QuestionChoiceLocalizedStore.DeleteStatement.DeleteQuery
                            deleteChoiceLocalizedQuery
                            = this.questionChoiceLocalizedStore
                            .delete()
                            .sql(deleteLocallizedChoiceSQL);

                    for (UUID cId: availableIds) {
                        deleteChoiceLocalizedQuery
                                .param(QuestionChoiceStore.id(cId));
                    }

                    deleteChoiceLocalizedQuery
                            .execute();

                    final String deleteChoiceSQL =
                            "DELETE FROM question_choice "
                                    + "WHERE question_id = ? AND id NOT IN ("
                                    + availableIds.stream()
                                    .map(aId -> "?")
                                    .collect(Collectors.joining(","))
                                    + ")";
                    availableIds.add(0, id);

                    QuestionChoiceStore.DeleteStatement.DeleteQuery
                            deleteChoiceQuery
                            = this.questionChoiceStore
                            .delete()
                            .sql(deleteChoiceSQL);

                    for (UUID cId: availableIds) {
                        deleteChoiceQuery.param(QuestionChoiceStore.id(cId));
                    }

                    deleteChoiceQuery
                            .execute();
                }


                for (QuestionChoice choice : question.getChoices()) {
                    if (choice.getId() == null) {
                        createChoice(choice, locale, id);
                    } else {
                        updateChoice(choice, locale);
                    }
                }

            }
            return updatedRows == 0 ? null : read(id, locale);
        } else {
            throw new ConstraintViolationException(violations);
        }


    }

    private void updateChoice(final QuestionChoice choice,
                              final Locale locale) throws SQLException {

        if (locale == null) {
            this.questionChoiceStore
                .update()
                .set(QuestionChoiceStore.cValue(choice.getCValue()),
                        QuestionChoiceStore.isAnswer(choice.getIsAnswer()))
                .where(QuestionChoiceStore.id().eq(choice.getId())).execute();
        } else {
            this.questionChoiceStore
                .update()
                .set(QuestionChoiceStore.isAnswer(choice.getIsAnswer()))
                .where(QuestionChoiceStore.id().eq(choice.getId()))
                    .execute();
            saveLocalizedChoice(locale, choice);
        }
    }

    /**
     * delete all records from questionchoice with the given question id.
     *
     * @param questionId
     */
    public void deleteChoices(final UUID questionId)
            throws SQLException {

        final String queryL =
                """
                        DELETE FROM question_choice_localized
                        WHERE choice_id IN
                        (SELECT id FROM question_choice WHERE question_id = ?)
                                """;
        this.questionChoiceLocalizedStore
                .delete()
                .sql(queryL)
                .param(QuestionChoiceStore.questionId(questionId)).execute();

        this.questionChoiceStore
                .delete(QuestionChoiceStore.questionId().eq(questionId))
                .execute();
    }


    /**
     * List question of exam.
     *
     * @param userName   the user name
     * @param categories the categories
     * @param locale     the locale
     * @return quetions in given exam
     */
    public List<Question> list(final String userName,
                               final Locale locale,
                               final List<String> categories)
            throws SQLException {

        boolean isOwner = userName.equals(OWNER_USER);

        List<com.gurukulams.questionbank.model.Question> qms;
        String query;

        if (locale == null) {
            query = "SELECT id,question,explanation,type,"
                    + (isOwner ? "answer" : "NULL")
                    + " AS answer,"
                    + "created_at,created_by,"
                    + "modified_at,modified_by"
                    + " FROM question"
                    + " where "
                    + "id IN (" + getQuestionIdFilter(categories) + ") "
                    + " order by id";
            QuestionStore.SelectStatement.SelectQuery queryBuilder
                    = this.questionStore.select()
                    .sql(query);

            for (String category: categories) {
                queryBuilder.param(QuestionCategoryStore.categoryId(category));
            }

            qms = queryBuilder
                    .list();
        } else {
            query = "SELECT id,"
                    + "CASE WHEN ql.LOCALE = ? "
                    + "THEN ql.question "
                    + "ELSE q.question "
                    + "END AS question,"
                    + "CASE WHEN ql.LOCALE = ? "
                    + "THEN ql.explanation "
                    + "ELSE q.explanation "
                    + "END AS explanation,"
                    + "type, created_by,"
                    + (isOwner ? "q.answer" : "NULL")
                    + " AS answer"
                    + "created_at,created_by,"
                    + "modified_at,modified_by"
                    + " FROM "
                    + "question q LEFT JOIN question_localized ql ON "
                    + "q.ID = ql.QUESTION_ID WHERE"
                    + " q.ID IN (" + getQuestionIdFilter(categories) + ") "
                    + "  AND"
                    + " (ql.LOCALE IS NULL "
                    + "OR ql.LOCALE = ? OR "
                    + "q.ID NOT IN "
                    + "(SELECT question_id FROM question_localized "
                    + "WHERE QUESTION_ID=q.ID AND LOCALE = ?))";

            QuestionStore.SelectStatement.SelectQuery queryBuilder
                    = this.questionStore.select()
                    .sql(query)
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()))
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()));

            for (String category: categories) {
                queryBuilder.param(QuestionCategoryStore.categoryId(category));
            }

            qms = queryBuilder
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()))
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()))
                    .list();

        }

        List<Question> questions = qms.stream().map(this::getQuestion)
                .toList();
        if (!questions.isEmpty()) {
            for (Question question : questions) {
                fillChoices(locale, question);
            }
        }
        return questions;

    }

    private String getQuestionIdFilter(final List<String> category) {
        return "SELECT QUESTION_ID FROM "
                + "question_category WHERE category_id IN ("
                + category.stream().map(tag -> "?")
                .collect(Collectors.joining(","))
                + ") "
                + "GROUP BY QUESTION_ID "
                + "HAVING COUNT(DISTINCT category_id) = "
                + category.size();
    }


    /**
     * Validate Question.
     *
     * @param question
     * @return violations
     */
    private Set<ConstraintViolation<Question>> getViolations(final Question
                                                                     question) {
        Set<ConstraintViolation<Question>> violations = new HashSet<>(validator
                .validate(question));
        if (violations.isEmpty()) {
            final String messageTemplate = null;
            final Class<Question> rootBeanClass = Question.class;
            final Object leafBeanInstance = null;
            final Object cValue = null;
            final Path propertyPath = null;
            final ConstraintDescriptor<?> constraintDescriptor = null;
            final ElementType elementType = null;
            final Map<String, Object> messageParameters = new HashMap<>();
            final Map<String, Object> expressionVariables = new HashMap<>();

            if (question.getType().equals(QuestionType.MATCH_THE_FOLLOWING)) {
                List<QuestionChoice> choices = question.getChoices();
                List<QuestionChoice> matches = question.getMatches();
                if (choices == null || choices.isEmpty()) {
                    ConstraintViolation<Question> violation
                            = ConstraintViolationImpl.forBeanValidation(
                            messageTemplate, messageParameters,
                            expressionVariables,
                            "No choices are provided",
                            rootBeanClass,
                            question, leafBeanInstance, cValue, propertyPath,
                            constraintDescriptor, elementType);
                    violations.add(violation);
                }
                if (matches == null || matches.isEmpty()) {
                    ConstraintViolation<Question> violation
                            = ConstraintViolationImpl.forBeanValidation(
                            messageTemplate, messageParameters,
                            expressionVariables,
                            "No matches are provided",
                            rootBeanClass,
                            question, leafBeanInstance, cValue, propertyPath,
                            constraintDescriptor, elementType);
                    violations.add(violation);
                }
                if (choices.size() > matches.size()) {
                    ConstraintViolation<Question> violation
                            = ConstraintViolationImpl.forBeanValidation(
                            messageTemplate, messageParameters,
                            expressionVariables,
                            "Not Enough Matches",
                            rootBeanClass,
                            question, leafBeanInstance, cValue, propertyPath,
                            constraintDescriptor, elementType);
                    violations.add(violation);
                }

            } else  if (question.getType().equals(QuestionType.MULTI_CHOICE)
                    || question.getType()
                    .equals(QuestionType.CHOOSE_THE_BEST)) {
                List<QuestionChoice> choices = question.getChoices();
                if (choices == null
                        || choices.size() < 2) {
                    ConstraintViolation<Question> violation
                            = ConstraintViolationImpl.forBeanValidation(
                            messageTemplate, messageParameters,
                            expressionVariables,
                            "Minimum 2 choices",
                            rootBeanClass,
                            question, leafBeanInstance, cValue, propertyPath,
                            constraintDescriptor, elementType);
                    violations.add(violation);
                } else if (choices.stream()
                        .filter(choice -> choice.getIsAnswer() != null
                                && choice.getIsAnswer())
                        .findFirst().isEmpty()) {
                    ConstraintViolation<Question> violation
                            = ConstraintViolationImpl.forBeanValidation(
                            messageTemplate, messageParameters,
                            expressionVariables,
                            "At-least One Answer should be available",
                            rootBeanClass,
                            question, leafBeanInstance, cValue, propertyPath,
                            constraintDescriptor, elementType);
                    violations.add(violation);
                }
            } else {
                if (question.getAnswer() == null) {
                    ConstraintViolation<Question> violation
                            = ConstraintViolationImpl.forBeanValidation(
                            messageTemplate, messageParameters,
                            expressionVariables,
                            "Answer should not be empty",
                            rootBeanClass,
                            question, leafBeanInstance, cValue, propertyPath,
                            constraintDescriptor, elementType);
                    violations.add(violation);
                }
            }
        }
        return violations;
    }

    /**
     * deletes from database.
     *
     * @param questionId   the questionId
     * @param questionType the questionType
     */
    public void delete(final UUID questionId,
                       final QuestionType questionType)
            throws SQLException {

        deleteChoices(questionId);

        this.questionLocalizedStore
                .delete(QuestionLocalizedStore.questionId().eq(questionId))
                .execute();

        this.questionCategoryStore
                .delete(QuestionCategoryStore.questionId().eq(questionId))
                .execute();

        this.questionStore
                .delete(QuestionStore.id().eq(questionId)
                        .and().type().eq(questionType.toString()))
                .execute();
    }


    /**
     * Adds tag to question.
     *
     * @param userName
     * @param questionId the questionId
     * @param categoryId the categoryId
     * @return grade optional
     */
    private boolean attachCategory(final String userName,
                                     final UUID questionId,
                                     final String categoryId)
            throws SQLException {



        int noOfRowsInserted = 0;

        QuestionCategory questionCategory = new QuestionCategory();
        questionCategory.setQuestionId(questionId);
        questionCategory.setCategoryId(categoryId);

        noOfRowsInserted = this.questionCategoryStore
                .insert()
                .values(questionCategory)
                .execute();

        return noOfRowsInserted == 1;
    }

    /**
     * Deletes Questions.
     */
    public void delete() throws SQLException {
        this.questionCategoryStore.delete().execute();
        this.questionTagStore.delete().execute();

        this.questionChoiceLocalizedStore.delete().execute();
        this.questionChoiceStore.delete().execute();

        this.questionLocalizedStore.delete().execute();
        this.questionStore.delete().execute();
    }
}
