package com.gurukulams.questionbank.util;

import com.gurukulams.questionbank.QuestionBankManager;
import org.postgresql.ds.PGSimpleDataSource;
public class TestUtil {
    public static QuestionBankManager questionBankManager() {
        PGSimpleDataSource ds = new PGSimpleDataSource() ;
        ds.setURL( "jdbc:postgresql://localhost:5432/gurukulams_questionbank" );
        ds.setUser( "tom" );
        ds.setPassword( "password" );
        return QuestionBankManager.getManager(ds);
    }

}
