BEGIN;

-- Thêm thể loại
INSERT INTO public.genres (name) VALUES
    ('Hoạt hình'),
    ('Phiêu lưu'),
    ('Hài'),
    ('Khoa học viễn tưởng'),
    ('Hành động'),
    ('Chính kịch');

-- Thêm phim mẫu
INSERT INTO public.movies
    (title, description, release_year, duration_minutes)
VALUES
    (
        'Toy Story',
        'Cuộc phiêu lưu của những món đồ chơi.',
        1995, 81
    ),
    (
        'Finding Nemo',
        'Hành trình đi tìm con của một chú cá hề.',
        2003, 100
    ),
    (
        'Interstellar',
        'Hành trình khám phá không gian để tìm nơi sống cho nhân loại.',
        2014, 169
    ),
    (
        'Inception',
        'Một nhóm chuyên gia thực hiện nhiệm vụ trong những giấc mơ.',
        2010, 148
    ),
    (
        'The Dark Knight',
        'Batman đối đầu với Joker tại thành phố Gotham.',
        2008, 152
    );

-- Gán thể loại cho phim bằng cách tra mã,
-- không giả định mã phim hoặc thể loại bắt đầu từ 1
INSERT INTO public.movie_genres (movie_id, genre_id)
SELECT m.movie_id, g.genre_id
FROM (
    VALUES
        ('Toy Story', 1995, 'Hoạt hình'),
        ('Toy Story', 1995, 'Phiêu lưu'),
        ('Toy Story', 1995, 'Hài'),
        ('Finding Nemo', 2003, 'Hoạt hình'),
        ('Finding Nemo', 2003, 'Phiêu lưu'),
        ('Interstellar', 2014, 'Khoa học viễn tưởng'),
        ('Interstellar', 2014, 'Phiêu lưu'),
        ('Interstellar', 2014, 'Chính kịch'),
        ('Inception', 2010, 'Khoa học viễn tưởng'),
        ('Inception', 2010, 'Hành động'),
        ('The Dark Knight', 2008, 'Hành động'),
        ('The Dark Knight', 2008, 'Chính kịch')
) AS sample(title, release_year, genre_name)
JOIN public.movies m
    ON m.title = sample.title
    AND m.release_year = sample.release_year
JOIN public.genres g
    ON g.name = sample.genre_name;

COMMIT;

-- Kiểm tra phim và các thể loại tương ứng
SELECT
    m.movie_id AS ma_phim,
    m.title AS ten_phim,
    m.release_year AS nam_phat_hanh,
    m.duration_minutes AS thoi_luong_phut,
    STRING_AGG(g.name, ', ' ORDER BY g.name) AS the_loai
FROM public.movies m
LEFT JOIN public.movie_genres mg
    ON mg.movie_id = m.movie_id
LEFT JOIN public.genres g
    ON g.genre_id = mg.genre_id
GROUP BY
    m.movie_id,
    m.title,
    m.release_year,
    m.duration_minutes
ORDER BY m.movie_id;