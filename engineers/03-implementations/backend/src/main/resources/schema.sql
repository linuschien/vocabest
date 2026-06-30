CREATE TABLE IF NOT EXISTS "user" (
    id UUID PRIMARY KEY,
    target_level VARCHAR(50),
    learning_streak INTEGER,
    daily_target_questions INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS daily_progress (
    id UUID PRIMARY KEY,
    user_id UUID,
    date TIMESTAMP,
    completed_questions INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_user_dp FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS vocabulary_word (
    id UUID PRIMARY KEY,
    word VARCHAR(255),
    part_of_speech VARCHAR(50),
    translation VARCHAR(255),
    level VARCHAR(50),
    exam_frequency INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quiz_question (
    id UUID PRIMARY KEY,
    vocabulary_word_id UUID,
    contextual_cloze VARCHAR(1000),
    translation VARCHAR(1000),
    correct_option VARCHAR(255),
    distractor1 VARCHAR(255),
    distractor2 VARCHAR(255),
    distractor3 VARCHAR(255),
    explanation_root_affix VARCHAR(500),
    explanation_mnemonic VARCHAR(500),
    target_level VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_vocab_word_qq FOREIGN KEY (vocabulary_word_id) REFERENCES vocabulary_word (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS error_log (
    id UUID PRIMARY KEY,
    user_id UUID,
    vocabulary_word_id UUID,
    quiz_question_id UUID,
    error_weight INTEGER,
    next_review_date TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_user_el FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_vocab_word_el FOREIGN KEY (vocabulary_word_id) REFERENCES vocabulary_word (id) ON DELETE SET NULL,
    CONSTRAINT fk_quiz_question_el FOREIGN KEY (quiz_question_id) REFERENCES quiz_question (id) ON DELETE SET NULL
);
