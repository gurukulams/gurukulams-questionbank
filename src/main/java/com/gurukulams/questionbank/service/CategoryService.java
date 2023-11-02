package com.gurukulams.questionbank.service;

import com.gurukulams.questionbank.QuestionBankManager;
import com.gurukulams.questionbank.model.Category;
import com.gurukulams.questionbank.model.CategoryLocalized;
import com.gurukulams.questionbank.store.CategoryLocalizedStore;
import com.gurukulams.questionbank.store.CategoryStore;
import com.gurukulams.questionbank.store.QuestionCategoryStore;


import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The type Category service.
 */
public class CategoryService {

    /**
     * categoryStore.
     */
    private final CategoryStore categoryStore;

    /**
     * categoryStore.
     */
    private final CategoryLocalizedStore categoryLocalizedStore;

    /**
     * questionCategoryStore.
     */
    private final QuestionCategoryStore questionCategoryStore;


    /**
     * Instantiates a new Category service.
     *
     * @param gurukulamsManager
     */
    public CategoryService(final QuestionBankManager gurukulamsManager) {
        this.categoryStore = gurukulamsManager.getCategoryStore();
        this.categoryLocalizedStore
                = gurukulamsManager.getCategoryLocalizedStore();
        this.questionCategoryStore
                = gurukulamsManager.getQuestionCategoryStore();
    }


    /**
     * Create category.
     *
     * @param userName the user name
     * @param locale   the locale
     * @param category the category
     * @return the category
     */
    public Category create(final String userName,
                           final Locale locale,
                           final Category category)
            throws SQLException {
        category.setCreatedBy(userName);
        this.categoryStore.insert().values(category).execute();

        if (locale != null) {
            create(category.getId(), category, locale);
        }

        return read(userName, category.getId(), locale).get();
    }

    private int create(final String categoryId,
                       final Category category,
                       final Locale locale) throws SQLException {

        CategoryLocalized categoryLocalized = new CategoryLocalized();
        categoryLocalized.setCategoryId(categoryId);
        categoryLocalized.setLocale(locale.getLanguage());
        categoryLocalized.setTitle(category.getTitle());
        return this.categoryLocalizedStore.insert()
                .values(categoryLocalized)
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
    public Optional<Category> read(final String userName,
                                   final String id,
                                   final Locale locale)
            throws SQLException {

        if (locale == null) {
            return this.categoryStore.select(id);
        }

        final String selectCategoryQuery =
                """
                        SELECT DISTINCT c.id,
                            CASE WHEN cl.LOCALE = ?
                                THEN cl.TITLE
                                ELSE c.TITLE
                            END AS TITLE,
                            created_at, created_by, modified_at, modified_by
                        FROM category c
                        LEFT JOIN category_localized cl ON c.ID = cl.CATEGORY_ID
                        WHERE c.ID = ?
                            AND (cl.LOCALE IS NULL
                            OR cl.LOCALE = ?
                            OR c.ID NOT IN (
                                SELECT CATEGORY_ID
                                FROM category_localized
                                WHERE CATEGORY_ID = c.ID
                                    AND LOCALE = ?
                            ))
                        """;


            return categoryStore.select().sql(selectCategoryQuery)
                            .param(CategoryLocalizedStore
                                    .locale(locale.getLanguage()))
                            .param(CategoryStore.id(id))
                            .param(CategoryLocalizedStore
                                    .locale(locale.getLanguage()))
                            .param(CategoryLocalizedStore
                                    .locale(locale.getLanguage()))
                            .optional();

    }

    /**
     * Update category.
     *
     * @param id       the id
     * @param userName the user name
     * @param locale   the locale
     * @param category the category
     * @return the category
     */
    public Category update(final String id,
                           final String userName,
                           final Locale locale,
                           final Category category) throws SQLException {


        int updatedRows = 0;

        if (locale == null) {
            updatedRows = this.categoryStore.update()
                    .set(CategoryStore.title(category.getTitle()),
                            CategoryStore.modifiedBy(userName))
                    .where(CategoryStore.id().eq(id)).execute();
        } else {
            updatedRows = this.categoryStore.update()
                    .set(CategoryStore.modifiedBy(userName))
                    .where(CategoryStore.id().eq(id)).execute();
            if (updatedRows != 0) {
                updatedRows = this.categoryLocalizedStore.update().set(
                        CategoryLocalizedStore.title(category.getTitle()),
                        CategoryLocalizedStore.locale(locale.getLanguage()))
                        .where(CategoryLocalizedStore.categoryId().eq(id)
                        .and().locale().eq(locale.getLanguage())).execute();

                if (updatedRows == 0) {
                    updatedRows = create(id, category, locale);
                }
            }
        }


        if (updatedRows == 0) {

            throw new IllegalArgumentException("Category not found");
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
    public List<Category> list(final String userName,
                               final Locale locale) throws SQLException {
        if (locale == null) {
            return this.categoryStore.select().execute();
        }
        final String listCategoryQuery =
                """
                        SELECT DISTINCT c.id,
                            CASE WHEN cl.LOCALE = ?
                                THEN cl.TITLE
                                ELSE c.TITLE
                            END AS TITLE,
                            created_at, created_by, modified_at, modified_by
                        FROM category c
                        LEFT JOIN category_localized cl ON c.ID = cl.CATEGORY_ID
                        WHERE cl.LOCALE IS NULL
                            OR cl.LOCALE = ?
                        """;

        return categoryStore.select().sql(listCategoryQuery)
                .param(CategoryLocalizedStore.locale(locale.getLanguage()))
                .param(CategoryLocalizedStore.locale(locale.getLanguage()))
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
        this.questionCategoryStore
                .delete(QuestionCategoryStore.categoryId().eq(id))
                .execute();
        this.categoryLocalizedStore
                .delete(CategoryLocalizedStore.categoryId().eq(id))
                .execute();
        return this.categoryStore.delete(id) == 1;
    }


    /**
     * Cleaning up all category.
     */
    public void delete() throws SQLException {
        this.questionCategoryStore.delete().execute();
        this.categoryLocalizedStore.delete().execute();
        this.categoryStore.delete().execute();
    }
}
