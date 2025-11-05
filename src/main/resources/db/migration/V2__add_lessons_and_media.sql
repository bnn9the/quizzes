-- Create media_assets table for file storage metadata
CREATE TABLE media_assets (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL UNIQUE,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create lessons table with optimistic locking
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    order_index INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Create junction table for course cover images
CREATE TABLE course_media_assets (
    course_id BIGINT NOT NULL,
    media_asset_id BIGINT NOT NULL,
    PRIMARY KEY (course_id, media_asset_id),
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (media_asset_id) REFERENCES media_assets(id) ON DELETE CASCADE
);

-- Create junction table for lesson media assets (embedded images)
CREATE TABLE lesson_media_assets (
    lesson_id BIGINT NOT NULL,
    media_asset_id BIGINT NOT NULL,
    PRIMARY KEY (lesson_id, media_asset_id),
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    FOREIGN KEY (media_asset_id) REFERENCES media_assets(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_media_assets_owner ON media_assets(owner_id);
CREATE INDEX idx_media_assets_checksum ON media_assets(checksum);
CREATE INDEX idx_lessons_course ON lessons(course_id);
CREATE INDEX idx_lessons_order ON lessons(course_id, order_index);
CREATE INDEX idx_course_media_assets_course ON course_media_assets(course_id);
CREATE INDEX idx_lesson_media_assets_lesson ON lesson_media_assets(lesson_id);

-- Add comment for documentation
COMMENT ON TABLE media_assets IS 'Stores metadata for uploaded files (images, documents)';
COMMENT ON TABLE lessons IS 'Structural elements of courses with optimistic locking';
COMMENT ON COLUMN lessons.version IS 'Version field for optimistic locking (@Version)';