CREATE TABLE IF NOT EXISTS "user" (
    id UUID PRIMARY KEY,
    email VARCHAR(255),
    role VARCHAR(50),
    target_level VARCHAR(50),
    daily_target_questions INTEGER,
    learning_streak INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS daily_progress (
    id UUID PRIMARY KEY,
    user_id UUID,
    date DATE,
    target_questions INTEGER,
    answered_questions INTEGER,
    correct_questions INTEGER,
    wrong_questions INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_user_dp FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS word_bank (
    id UUID PRIMARY KEY,
    word VARCHAR(255),
    parts_of_speech VARCHAR(50),
    chinese_translation TEXT,
    target_level VARCHAR(50),
    difficulty_level INTEGER,
    exam_frequency INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quiz_question (
    id UUID PRIMARY KEY,
    word_bank_id UUID,
    contextual_cloze TEXT,
    chinese_translation TEXT,
    correct_answer VARCHAR(255),
    distractor1 VARCHAR(255),
    distractor2 VARCHAR(255),
    distractor3 VARCHAR(255),
    explanation_root_affix TEXT,
    explanation_mnemonic TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_vocab_word_qq FOREIGN KEY (word_bank_id) REFERENCES word_bank (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS word_mastery (
    id UUID PRIMARY KEY,
    user_id UUID,
    word_bank_id UUID,
    error_weight INTEGER,
    next_review_date TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_user_wm FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_word_bank_wm FOREIGN KEY (word_bank_id) REFERENCES word_bank (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS error_event (
    id UUID PRIMARY KEY,
    user_id UUID,
    quiz_question_id UUID,
    timestamp TIMESTAMP,
    selected_distractor VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_user_ee FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_quiz_question_ee FOREIGN KEY (quiz_question_id) REFERENCES quiz_question (id) ON DELETE SET NULL
);
