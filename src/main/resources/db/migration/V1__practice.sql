CREATE TABLE category (
    id VARCHAR(55) PRIMARY KEY,
    title TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(55) NOT NULL,
    modified_at TIMESTAMP,
    modified_by VARCHAR(200),
    CONSTRAINT category_title_constraint UNIQUE (title)
);

CREATE TABLE category_localized (
    category_id VARCHAR(55),
    locale VARCHAR(8) NOT NULL,
    title TEXT,
    FOREIGN KEY (category_id) REFERENCES category (id),
    PRIMARY KEY(category_id, locale)
);

CREATE TABLE tag (
    id VARCHAR(55) PRIMARY KEY,
    title TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(55) NOT NULL,
    modified_at TIMESTAMP,
    modified_by VARCHAR(200),
    CONSTRAINT tag_title_constraint UNIQUE (title)
);

CREATE TABLE tag_localized (
    tag_id VARCHAR(55),
    locale VARCHAR(8) NOT NULL,
    title TEXT,
    FOREIGN KEY (tag_id) REFERENCES tag (id),
    PRIMARY KEY(tag_id, locale)
);

CREATE TABLE question (
  id UUID PRIMARY KEY,
  question TEXT NOT NULL,
  explanation TEXT NOT NULL,
  type VARCHAR(55) NOT NULL,
  answer VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(55) NOT NULL,
  modified_at TIMESTAMP,
  modified_by VARCHAR(200)
);

CREATE TABLE question_localized (
    question_id UUID,
    locale VARCHAR(8) NOT NULL,
    question TEXT NOT NULL,
    explanation TEXT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES question (id),
    PRIMARY KEY(question_id, locale)
);

CREATE TABLE question_choice (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL,
    c_value VARCHAR NOT NULL,
    is_answer BOOLEAN,
    FOREIGN KEY (question_id) REFERENCES question (id)
);

CREATE TABLE question_choice_localized (
    choice_id UUID,
    locale VARCHAR(8) NOT NULL,
    c_value VARCHAR NOT NULL,
    FOREIGN KEY (choice_id) REFERENCES question_choice (id),
    PRIMARY KEY (choice_id, locale)
);

CREATE TABLE answer (
  id UUID PRIMARY KEY,
  exam_id UUID NOT NULL,
  question_id UUID NOT NULL,
  student_answer VARCHAR(500) NOT NULL,
  FOREIGN KEY (question_id) REFERENCES question (id)
);


CREATE TABLE question_category (
    question_id UUID NOT NULL,
    category_id VARCHAR(55) NOT NULL,
    PRIMARY KEY(question_id, category_id),
    FOREIGN KEY (question_id) REFERENCES question (id),
    FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE question_tag (
    question_id UUID NOT NULL,
    tag_id VARCHAR(55) NOT NULL,
    PRIMARY KEY(question_id, tag_id),
    FOREIGN KEY (question_id) REFERENCES question (id),
    FOREIGN KEY (tag_id) REFERENCES tag (id)
);
