create table matches (
    question_id UUID not null,
    choice_id UUID,
    match_id UUID not null,
    primary key (question_id,
    choice_id,
    match_id),
    foreign key (question_id) references question (id),
    foreign key (choice_id) references question_choice (id),
    foreign key (match_id) references question_choice (id)
);