CREATE DATABASE IF NOT EXISTS yandex;

CREATE TABLE IF NOT EXISTS yandex.market_category_models
(
    timestamp DateTime DEFAULT toDateTime(now(), 'Europe/Moscow') CODEC(DoubleDelta),
    uri                String,
    host               LowCardinality(String),
    path               LowCardinality(String),
    body_text          String
)
ENGINE = MergeTree()
ORDER BY (timestamp, host, path, uri);
