BEGIN;

-- 1. Tài khoản
CREATE TABLE public.users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_users_username
        CHECK (
            username = LOWER(username)
            AND username ~ '^[a-z0-9_]{3,50}$'
        ),
    CONSTRAINT ck_users_full_name
        CHECK (LENGTH(TRIM(full_name)) > 0),
    CONSTRAINT ck_users_role
        CHECK (role IN ('ADMIN', 'USER'))
);

-- 2. Phim
CREATE TABLE public.movies (
    movie_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    release_year INTEGER,
    duration_minutes INTEGER,
    poster_path VARCHAR(500),
    movielens_id INTEGER UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_movies_title
        CHECK (LENGTH(TRIM(title)) > 0),
    CONSTRAINT ck_movies_year
        CHECK (release_year BETWEEN 1888 AND 2100),
    CONSTRAINT ck_movies_duration
        CHECK (duration_minutes > 0),
    CONSTRAINT ck_movies_movielens
        CHECK (movielens_id > 0)
);

-- 3. Thể loại
CREATE TABLE public.genres (
    genre_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,

    CONSTRAINT ck_genres_name
        CHECK (LENGTH(TRIM(name)) > 0)
);

-- Không cho trùng thể loại do khác chữ hoa/thường hoặc khoảng trắng
CREATE UNIQUE INDEX uq_genres_name
    ON public.genres (LOWER(TRIM(name)));

-- 4. Mỗi phim có thể thuộc nhiều thể loại
CREATE TABLE public.movie_genres (
    movie_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,

    PRIMARY KEY (movie_id, genre_id),

    CONSTRAINT fk_movie_genres_movie
        FOREIGN KEY (movie_id)
        REFERENCES public.movies(movie_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_movie_genres_genre
        FOREIGN KEY (genre_id)
        REFERENCES public.genres(genre_id)
        ON DELETE RESTRICT
);

-- 5. Đánh giá
CREATE TABLE public.ratings (
    rating_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    score SMALLINT NOT NULL,
    comment TEXT,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_ratings_user_movie
        UNIQUE (user_id, movie_id),

    CONSTRAINT ck_ratings_score
        CHECK (score BETWEEN 1 AND 5),

    CONSTRAINT fk_ratings_user
        FOREIGN KEY (user_id)
        REFERENCES public.users(user_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_ratings_movie
        FOREIGN KEY (movie_id)
        REFERENCES public.movies(movie_id)
        ON DELETE RESTRICT
);

-- Hỗ trợ tra cứu đánh giá theo phim, lọc phim theo thể loại
CREATE INDEX idx_ratings_movie_id
    ON public.ratings(movie_id);

CREATE INDEX idx_movie_genres_genre_id
    ON public.movie_genres(genre_id);

COMMIT;

-- Kiểm tra các bảng vừa tạo
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;