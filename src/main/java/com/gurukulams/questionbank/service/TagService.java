package com.gurukulams.questionbank.service;

import com.gurukulams.questionbank.QuestionBankManager;
import com.gurukulams.questionbank.model.Tag;
import com.gurukulams.questionbank.model.TagLocalized;
import com.gurukulams.questionbank.store.TagLocalizedStore;
import com.gurukulams.questionbank.store.TagStore;
import com.gurukulams.questionbank.store.QuestionTagStore;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The type Tag service.
 */
public class TagService {

    /**
     * tagStore.
     */
    private final TagStore tagStore;

    /**
     * tagStore.
     */
    private final TagLocalizedStore tagLocalizedStore;

    /**
     * questionTagStore.
     */
    private final QuestionTagStore questionTagStore;


    /**
     * Instantiates a new Tag service.
     *
     * @param gurukulamsManager
     */
    public TagService(final QuestionBankManager gurukulamsManager) {
        this.tagStore = gurukulamsManager.getTagStore();
        this.tagLocalizedStore
                = gurukulamsManager.getTagLocalizedStore();
        this.questionTagStore
                = gurukulamsManager.getQuestionTagStore();
    }


    /**
     * Create tag.
     *
     * @param userName the user name
     * @param locale   the locale
     * @param tag the tag
     * @return the tag
     */
    public Tag create(final String userName,
                           final Locale locale,
                           final Tag tag)
            throws SQLException {
        tag.setCreatedBy(userName);
        this.tagStore.insert().values(tag).execute();

        if (locale != null) {
            create(tag.getId(), tag, locale);
        }

        return read(userName, tag.getId(), locale).get();
    }

    private int create(final String tagId,
                       final Tag tag,
                       final Locale locale) throws SQLException {

        TagLocalized tagLocalized = new TagLocalized();
        tagLocalized.setTagId(tagId);
        tagLocalized.setLocale(locale.getLanguage());
        tagLocalized.setTitle(tag.getTitle());
        return this.tagLocalizedStore.insert()
                .values(tagLocalized)
                .execute();
    }

    /**
     * Read optional.
     *
     * @param userName the user name
     * @param id       the id
     * @param locale   the locale
     * @return the optional
     */
    public Optional<Tag> read(final String userName,
                                   final String id,
                                   final Locale locale)
            throws SQLException {

        if (locale == null) {
            return this.tagStore.select(id);
        }

        final String selectTagQuery =
                """
                        SELECT DISTINCT c.id,
                            CASE WHEN cl.LOCALE = ?
                                THEN cl.TITLE
                                ELSE c.TITLE
                            END AS TITLE,
                            created_at, created_by, modified_at, modified_by
                        FROM tag c
                        LEFT JOIN tag_localized cl ON c.ID = cl.TAG_ID
                        WHERE c.ID = ?
                            AND (cl.LOCALE IS NULL
                            OR cl.LOCALE = ?
                            OR c.ID NOT IN (
                                SELECT TAG_ID
                                FROM tag_localized
                                WHERE TAG_ID = c.ID
                                    AND LOCALE = ?
                            ))
                        """;


            return tagStore.select().sql(selectTagQuery)
                            .param(TagLocalizedStore
                                    .locale(locale.getLanguage()))
                            .param(TagStore.id(id))
                            .param(TagLocalizedStore
                                    .locale(locale.getLanguage()))
                            .param(TagLocalizedStore
                                    .locale(locale.getLanguage()))
                            .optional();

    }

    /**
     * Update tag.
     *
     * @param id       the id
     * @param userName the user name
     * @param locale   the locale
     * @param tag the tag
     * @return the tag
     */
    public Tag update(final String id,
                           final String userName,
                           final Locale locale,
                           final Tag tag) throws SQLException {


        int updatedRows = 0;

        if (locale == null) {
            updatedRows = this.tagStore.update()
                    .set(TagStore.title(tag.getTitle()),
                            TagStore.modifiedBy(userName))
                    .where(TagStore.id().eq(id)).execute();
        } else {
            updatedRows = this.tagStore.update()
                    .set(TagStore.modifiedBy(userName))
                    .where(TagStore.id().eq(id)).execute();
            if (updatedRows != 0) {
                updatedRows = this.tagLocalizedStore.update().set(
                        TagLocalizedStore.title(tag.getTitle()),
                        TagLocalizedStore.locale(locale.getLanguage()))
                        .where(TagLocalizedStore.tagId().eq(id)
                        .and().locale().eq(locale.getLanguage())).execute();

                if (updatedRows == 0) {
                    updatedRows = create(id, tag, locale);
                }
            }
        }


        if (updatedRows == 0) {

            throw new IllegalArgumentException("tag not found");
        }

        return read(userName, id, locale).get();
    }



    /**
     * List list.
     *
     * @param userName the user name
     * @param locale   the locale
     * @return the list
     */
    public List<Tag> list(final String userName,
                               final Locale locale) throws SQLException {
        if (locale == null) {
            return this.tagStore.select().execute();
        }
        final String listTagQuery =
                """
                        SELECT DISTINCT c.id,
                            CASE WHEN cl.LOCALE = ?
                                THEN cl.TITLE
                                ELSE c.TITLE
                            END AS TITLE,
                            created_at, created_by, modified_at, modified_by
                        FROM tag c
                        LEFT JOIN tag_localized cl ON c.ID = cl.TAG_ID
                        WHERE cl.LOCALE IS NULL
                            OR cl.LOCALE = ?
                        """;

        return tagStore.select().sql(listTagQuery)
                .param(TagLocalizedStore.locale(locale.getLanguage()))
                .param(TagLocalizedStore.locale(locale.getLanguage()))
                .list();
    }


    /**
     * Delete boolean.
     *
     * @param userName the user name
     * @param id       the id
     * @return the boolean
     */
    public boolean delete(final String userName, final String id)
            throws SQLException {
        this.questionTagStore
                .delete(QuestionTagStore.tagId().eq(id))
                .execute();
        this.tagLocalizedStore
                .delete(TagLocalizedStore.tagId().eq(id))
                .execute();
        return this.tagStore.delete(id) == 1;
    }


    /**
     * Cleaning up all Tag.
     */
    public void delete() throws SQLException {
        this.questionTagStore.delete().execute();
        this.tagLocalizedStore.delete().execute();
        this.tagStore.delete().execute();
    }
}
