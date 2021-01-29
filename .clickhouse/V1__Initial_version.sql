CREATE DATABASE IF NOT EXISTS marketplace;

USE marketplace;

CREATE TABLE IF NOT EXISTS ozon_category_search_results_v2_items_stream
(
    timestamp            DateTime64(3, 'Europe/Moscow'),
    itemId               UInt64,
    itemType             Enum('sku' = 1),
    itemTitle            String,
    brandId              UInt64,
    brandName            String,
    priceBase            UInt32,
    priceFinal           UInt32,
    pricePercentDiscount UInt8,
    ratingValue          Float64,
    ratingCount          UInt32,
    categoryPath         String,
    deliverySchema       Enum('FBO' = 1, 'FBS' = 2, 'Retail' = 3, 'Crossborder' = 4),
    deliveryTimeDiffDays Int16,
    availability         UInt8,
    availableInDays      Int16,
    marketplaceSellerId  UInt64,
    addToCartMinItems    Nullable(Int32),
    addToCartMaxItems    Nullable(Int32),
    isAdult              UInt8,
    isAlcohol            UInt8,
    isSupermarket        UInt8,
    isPersonalized       UInt8,
    isPromotedProduct    UInt8,
    index                UInt32,
    freeRest             Int32
)
ENGINE = Kafka SETTINGS kafka_broker_list = 'broker:29092',
                        kafka_topic_list = 'parser-results-ozon_category_search_results_v2_items',
                        kafka_group_name = 'ozon_category_search_results_v2_items',
                        kafka_format = 'AvroConfluent',
                        kafka_num_consumers = 10,
                        kafka_commit_every_batch = 1,
                        format_avro_schema_registry_url = 'http://schema-registry:8081';

CREATE TABLE IF NOT EXISTS ozon_category_search_results_v2_items
(
    timestamp               DateTime64(3, 'Europe/Moscow') CODEC(DoubleDelta),
    item_id                 UInt64,
    item_type               Enum('sku' = 1),
    item_title              String,
    brand_id                UInt64,
    brand_name              String,
    price_base              UInt32,
    price_final             UInt32,
    price_percent_discount  UInt8,
    rating_value            Float64,
    rating_count            UInt32,
    category_path           String,
    delivery_schema         Enum('FBO' = 1, 'FBS' = 2, 'Retail' = 3, 'Crossborder' = 4),
    delivery_time_diff_days Int16,
    availability            UInt8,
    available_in_days       Int16,
    marketplace_seller_id   UInt64,
    add_to_cart_min_items   Nullable(Int32),
    add_to_cart_max_items   Nullable(Int32),
    is_adult                UInt8,
    is_alcohol              UInt8,
    is_supermarket          UInt8,
    is_personalized         UInt8,
    is_promoted_product     UInt8,
    index                   UInt32,
    free_rest               Int32
) ENGINE = MergeTree() ORDER     BY (timestamp, item_id, item_type)
                       PARTITION BY toYYYYMM(timestamp);


CREATE MATERIALIZED VIEW IF NOT EXISTS ozon_category_search_results_v2_items_consumer
TO ozon_category_search_results_v2_items
AS
    SELECT
        timestamp            AS timestamp,
        itemId               AS item_id,
        itemType             AS item_type,
        itemTitle            AS item_title,
        brandId              AS brand_id,
        brandName            AS brand_name,
        priceBase            AS price_base,
        priceFinal           AS price_final,
        pricePercentDiscount AS price_percent_discount,
        ratingValue          AS rating_value,
        ratingCount          AS rating_count,
        categoryPath         AS category_path,
        deliverySchema       AS delivery_schema,
        deliveryTimeDiffDays AS delivery_time_diff_days,
        availability         AS availability,
        availableInDays      AS available_in_days,
        marketplaceSellerId  AS marketplace_seller_id,
        addToCartMinItems    AS add_to_cart_min_items,
        addToCartMaxItems    AS add_to_cart_max_items,
        isAdult              AS is_adult,
        isAlcohol            AS is_alcohol,
        isSupermarket        AS is_supermarket,
        isPersonalized       AS is_personalized,
        isPromotedProduct    AS is_promoted_product,
        index                AS index,
        freeRest             AS free_rest
    FROM ozon_category_search_results_v2_items_stream;
