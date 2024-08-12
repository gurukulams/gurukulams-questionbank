create table matches (
    question_id UUID references question_choice (id) not null,
    choice_id UUID references question_choice (id),
    match_id UUID references question_choice (id) not null,
    foreign key (question_id) references question (id),
    choice_id references question_choice (id),
    foreign key (match_id) references question_choice (id)
);