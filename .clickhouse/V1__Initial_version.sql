CREATE DATABASE IF NOT EXISTS dalytics;

CREATE TABLE IF NOT EXISTS dalytics.ozon_category_search_results_v2_items_stream
ON CLUSTER cluster
(
    timestamp            DateTime64(3, 'Europe/Moscow'),
    itemId               UInt64,
    itemIndex            UInt32,
    itemType             Enum('sku' = 1),
    itemTitle            String,
    brandId              UInt64,
    brandName            String,
    priceBase            Float64,
    priceFinal           Float64,
    pricePercentDiscount UInt8,
    ratingValue          Float64,
    ratingCount          UInt32,
    categoryId           UInt64,
    categoryName         LowCardinality(String),
    categoryCatalogName  Nullable(String),
    currentPage          UInt32,
    totalPages           UInt32,
    totalFoundItems      UInt32,
    categoryPath         String,
    deliverySchema       Enum('FBO' = 1, 'FBS' = 2, 'Retail' = 3, 'Crossborder' = 4),
    deliveryTimeDiffDays Int16,
    availability         UInt8,
    availableInDays      Int16,
    marketplaceSellerId  UInt64,
    addToCartIsRedirect  Nullable(UInt8),
    addToCartMinItems    Nullable(Int32),
    addToCartMaxItems    Nullable(Int32),
    numberOfSoldItems    Nullable(Int32),
    isAdult              UInt8,
    isAlcohol            UInt8,
    isAvailable          UInt8,
    isSupermarket        UInt8,
    isPersonalized       UInt8,
    isPromotedProduct    UInt8,
    freeRest             Int32
) ENGINE = Kafka SETTINGS kafka_broker_list = 'broker:29092',
                          kafka_topic_list = 'marketplace_enricher-results-ozon_category_search_results_v2_items-version_1',
                          kafka_group_name = 'clickhouse-ozon_category_search_results_v2_items',
                          kafka_format = 'AvroConfluent',
                          kafka_commit_every_batch = 1,
                          kafka_max_block_size = 1048576,
                          kafka_num_consumers = 8,
                          kafka_thread_per_consumer = 1,
                          format_avro_schema_registry_url = 'http://schema-registry:8081';

CREATE TABLE IF NOT EXISTS dalytics.ozon_category_search_results_v2_items
ON CLUSTER cluster
(
    timestamp               DateTime64(3, 'Europe/Moscow') CODEC(DoubleDelta),
    time                    DateTime('Europe/Moscow')      CODEC(Delta, LZ4),
    item_id                 UInt64,
    item_index              UInt32,
    item_type               Enum('sku' = 1),
    item_title              String,
    brand_id                UInt64,
    brand_name              LowCardinality(String),
    price_base              Decimal64(2),
    price_final             Decimal64(2),
    price_percent_discount  UInt8,
    rating_value            Float64,
    rating_count            UInt32,
    category_id             UInt64,
    category_name           LowCardinality(String),
    category_catalog_name   Nullable(String),
    current_page            UInt32,
    total_pages             UInt32,
    total_found_items       UInt32,
    category_path           String,
    delivery_schema         Enum('FBO' = 1, 'FBS' = 2, 'Retail' = 3, 'Crossborder' = 4),
    delivery_time_diff_days Int16,
    availability            UInt8,
    available_in_days       Int16,
    marketplace_seller_id   UInt64,
    add_to_cart_is_redirect Nullable(UInt8),
    add_to_cart_min_items   Nullable(Int32),
    add_to_cart_max_items   Nullable(Int32),
    number_of_sold_items    Nullable(Int32),
    is_adult                UInt8,
    is_alcohol              UInt8,
    is_available            UInt8,
    is_supermarket          UInt8,
    is_personalized         UInt8,
    is_promoted_product     UInt8,
    free_rest               Int32
) ENGINE = ReplicatedReplacingMergeTree('/clickhouse/tables/{database}/{table}', '{replica}', time)
           ORDER     BY (category_id, brand_id, item_id, time)
           PARTITION BY toYYYYMM(timestamp);

CREATE MATERIALIZED VIEW IF NOT EXISTS dalytics.ozon_category_search_results_v2_items_consumer
ON CLUSTER cluster
TO dalytics.ozon_category_search_results_v2_items
AS
    SELECT
        timestamp                              AS timestamp,
        toDateTime(timestamp, 'Europe/Moscow') AS time,
        itemId                                 AS item_id,
        itemIndex                              AS item_index,
        itemType                               AS item_type,
        itemTitle                              AS item_title,
        brandId                                AS brand_id,
        brandName                              AS brand_name,
        toDecimal64(priceBase, 2)              AS price_base,
        toDecimal64(priceFinal, 2)             AS price_final,
        pricePercentDiscount                   AS price_percent_discount,
        ratingValue                            AS rating_value,
        ratingCount                            AS rating_count,
        categoryId                             AS category_id,
        categoryName                           AS category_name,
        categoryCatalogName                    AS category_catalog_name,
        currentPage                            AS current_page,
        totalPages                             AS total_pages,
        totalFoundItems                        AS total_found_items,
        categoryPath                           AS category_path,
        deliverySchema                         AS delivery_schema,
        deliveryTimeDiffDays                   AS delivery_time_diff_days,
        availability                           AS availability,
        availableInDays                        AS available_in_days,
        marketplaceSellerId                    AS marketplace_seller_id,
        addToCartIsRedirect                    AS add_to_cart_is_redirect,
        addToCartMinItems                      AS add_to_cart_min_items,
        addToCartMaxItems                      AS add_to_cart_max_items,
        numberOfSoldItems                      AS number_of_sold_items,
        isAdult                                AS is_adult,
        isAlcohol                              AS is_alcohol,
        isAvailable                            AS is_available,
        isSupermarket                          AS is_supermarket,
        isPersonalized                         AS is_personalized,
        isPromotedProduct                      AS is_promoted_product,
        freeRest                               AS free_rest
    FROM dalytics.ozon_category_search_results_v2_items_stream;

CREATE TABLE IF NOT EXISTS dalytics.ozon_seller_list_items_stream
ON CLUSTER cluster
(
    timestamp      DateTime64(3, 'Europe/Moscow'),
    sellerId       UInt64,
    sellerTitle    String,
    sellerSubtitle String
) ENGINE = Kafka SETTINGS kafka_broker_list = 'broker:29092',
                          kafka_topic_list = 'marketplace_parser-results-ozon_seller_list_items-version_1',
                          kafka_group_name = 'clickhouse-ozon_seller_list_items',
                          kafka_format = 'AvroConfluent',
                          kafka_commit_every_batch = 1,
                          kafka_max_block_size = 1048576,
                          kafka_num_consumers = 2,
                          kafka_thread_per_consumer = 1,
                          format_avro_schema_registry_url = 'http://schema-registry:8081';

CREATE TABLE IF NOT EXISTS dalytics.ozon_seller_list_items
ON CLUSTER cluster
(
    updated_at DateTime('Europe/Moscow') CODEC(Delta, LZ4),
    id         UInt64,
    title      String,
    subtitle   String
) ENGINE = ReplicatedReplacingMergeTree('/clickhouse/tables/{database}/{table}', '{replica}', updated_at)
           ORDER     BY id
           PARTITION BY toYYYYMM(updated_at);

CREATE MATERIALIZED VIEW IF NOT EXISTS dalytics.ozon_seller_list_items_consumer
ON CLUSTER cluster
TO dalytics.ozon_seller_list_items
AS
    SELECT
        toDateTime(timestamp, 'Europe/Moscow') AS updated_at,
        sellerId                               AS id,
        sellerTitle                            AS title,
        sellerSubtitle                         AS subtitle
    FROM dalytics.ozon_seller_list_items_stream;
