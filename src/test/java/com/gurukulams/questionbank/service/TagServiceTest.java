package com.gurukulams.questionbank.service;

import com.gurukulams.questionbank.model.Tag;
import com.gurukulams.questionbank.util.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

class TagServiceTest {


    private final TagService tagService;

    TagServiceTest() {
        this.tagService = new TagService(TestUtil.questionBankManager());
    }

    /**
     * Before.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void before() throws IOException, SQLException {
        cleanUp();
    }

    /**
     * After.
     */
    @AfterEach
    void after() throws SQLException {
        cleanUp();
    }

    private void cleanUp() throws SQLException {
        tagService.delete();
    }


    @Test
    void create() throws SQLException {
        final Tag tag = tagService.create("hari"
                , null, anTag());
        Assertions.assertTrue(tagService.read("hari", tag.getId(), null).isPresent(), "Created Tag");
    }

    @Test
    void createLocalized() throws SQLException {
        final Tag tag = tagService.create("hari"
                , Locale.GERMAN, anTag());
        Assertions.assertTrue(tagService.read("hari", tag.getId(), Locale.GERMAN).isPresent(), "Created Localized Tag");
        Assertions.assertTrue(tagService.read("hari", tag.getId(), null).isPresent(), "Created Tag");
    }

    @Test
    void read() throws SQLException {
        final Tag tag = tagService.create("hari",
                null, anTag());
        Assertions.assertTrue(tagService.read("hari", tag.getId(), null).isPresent(),
                "Created Tag");
    }

    @Test
    void update() throws SQLException {

        final Tag tag = tagService.create("hari",
                null, anTag());
        Tag newTag = new Tag();
        newTag.setId(tag.getId());
         newTag.setTitle("HansiTag");
        Tag updatedTag = tagService
                .update(tag.getId(), "priya", null, newTag);
        Assertions.assertEquals("HansiTag", updatedTag.getTitle(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tagService
                    .update(UUID.randomUUID().toString(), "priya", null, newTag);
        });
    }

    @Test
    void updateLocalized() throws SQLException {

        final Tag tag = tagService.create("hari",
                null, anTag());
        Tag newTag = new Tag();
        newTag.setId(tag.getId());
        newTag.setTitle("HansiTag");
        Tag updatedTag = tagService
                .update(tag.getId(), "priya", Locale.GERMAN, newTag);

        Assertions.assertEquals("HansiTag", tagService.read("mani", tag.getId(), Locale.GERMAN).get().getTitle(), "Updated");
        Assertions.assertNotEquals("HansiTag", tagService.read("mani", tag.getId(), null).get().getTitle(), "Updated");


        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tagService
                    .update(UUID.randomUUID().toString(), "priya", null, newTag);
        });
    }

    @Test
    void delete() throws SQLException {

        final Tag tag = tagService.create("hari", null,
                anTag());
        tagService.delete("mani", tag.getId());
        Assertions.assertFalse(tagService.read("mani", tag.getId(), null).isPresent(), "Deleted Tag");
    }

    @Test
    void list() throws SQLException {

        final Tag tag = tagService.create("hari", null,
                anTag());
        Tag newTag = new Tag();
        newTag.setId(UUID.randomUUID().toString());
        newTag.setTitle("HansiTag");
        tagService.create("hari", null,
                newTag);
        List<Tag> listofcategories = tagService.list("hari", null);
        Assertions.assertEquals(2, listofcategories.size());

    }

    @Test
    void listLocalized() throws SQLException {

        final Tag tag = tagService.create("hari", Locale.GERMAN,
                anTag());
        Tag newTag = new Tag();
        newTag.setId(UUID.randomUUID().toString());
        newTag.setTitle("HansiTag");
        tagService.create("hari", null,
                newTag);
        List<Tag> listofcategories = tagService.list("hari", null);
        Assertions.assertEquals(2, listofcategories.size());

        listofcategories = tagService.list("hari", Locale.GERMAN);
        Assertions.assertEquals(2, listofcategories.size());

    }


    /**
     * Gets practice.
     *
     * @return the practice
     */
    Tag anTag() {

        Tag tag = new Tag();
        tag.setId(UUID.randomUUID().toString());
        tag.setTitle("HariTag");
        return tag;
    }


}