CREATE DATABASE IF NOT EXISTS marketplaces;

CREATE TABLE IF NOT EXISTS yandex_market_responses
(
    timestamp DateTime DEFAULT toDateTime(now(), 'Europe/Moscow') CODEC(DoubleDelta),
    uri                String,
    host               LowCardinality(String),
    path               LowCardinality(String),
    body_text          String
)
ENGINE = MergeTree()
ORDER BY (timestamp, host, path, uri);
