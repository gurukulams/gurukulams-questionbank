module gurukulams.questionbank {
    requires java.base;
    requires java.sql;
    requires java.naming;
    requires jakarta.validation;
    requires org.hibernate.validator;
    requires com.h2database;

    opens com.gurukulams.questionbank.service;
    opens com.gurukulams.questionbank.payload;
    opens db.upgrades;

    exports com.gurukulams.questionbank.service;
    exports com.gurukulams.questionbank.payload;
    exports com.gurukulams.questionbank.model;
    exports com.gurukulams.questionbank;
}