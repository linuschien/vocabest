CREATE TABLE IF NOT EXISTS "user" (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    target_level VARCHAR(50) NOT NULL,
    daily_target_questions INTEGER NOT NULL,
    learning_streak INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS daily_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    date DATE NOT NULL,
    target_questions INTEGER NOT NULL,
    answered_questions INTEGER NOT NULL,
    correct_questions INTEGER NOT NULL,
    wrong_questions INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_user_dp FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT uq_daily_progress_user_date UNIQUE (user_id, date)
);

CREATE TABLE IF NOT EXISTS word_bank (
    id UUID PRIMARY KEY,
    word VARCHAR(255) NOT NULL,
    parts_of_speech VARCHAR(50) NOT NULL,
    chinese_translation TEXT NOT NULL,
    target_level VARCHAR(50) NOT NULL,
    difficulty_level INTEGER NOT NULL,
    exam_frequency INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_word_bank_word_level UNIQUE (word, target_level)
);

CREATE TABLE IF NOT EXISTS quiz_question (
    id UUID PRIMARY KEY,
    word_bank_id UUID NOT NULL,
    contextual_cloze TEXT NOT NULL,
    chinese_translation TEXT NOT NULL,
    correct_answer VARCHAR(255) NOT NULL,
    distractor1 VARCHAR(255) NOT NULL,
    distractor2 VARCHAR(255) NOT NULL,
    distractor3 VARCHAR(255) NOT NULL,
    explanation_root_affix TEXT NOT NULL,
    explanation_mnemonic TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_vocab_word_qq FOREIGN KEY (word_bank_id) REFERENCES word_bank (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS word_mastery (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    word_bank_id UUID NOT NULL,
    error_weight INTEGER NOT NULL,
    next_review_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_user_wm FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_word_bank_wm FOREIGN KEY (word_bank_id) REFERENCES word_bank (id) ON DELETE CASCADE,
    CONSTRAINT uq_word_mastery_user_word UNIQUE (user_id, word_bank_id)
);

CREATE TABLE IF NOT EXISTS error_event (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    quiz_question_id UUID NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    selected_distractor VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_user_ee FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_quiz_question_ee FOREIGN KEY (quiz_question_id) REFERENCES quiz_question (id) ON DELETE CASCADE
);

