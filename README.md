# 書籍API

このプロジェクトは、Spring BootとKotlinで構築された、書籍と著者を管理するためのRESTful APIです。書籍と著者間の多対多の関係をサポートし、基本的なCRUD操作を提供します。

## 機能 (Features)

本プロジェクトは、書籍と著者に関する以下の主要機能を提供します。

- **著者管理:** 著者の情報の作成、取得、更新、全取得。
- **書籍管理:** 書籍情報の作成、取得、更新、特定の著者に関連する書籍の取得。
- **データ検証:** 書籍や著者エンティティのデータ整合性を保証するバリデーション。
- **グローバル例外処理:** 一貫したエラーレスポンスを提供する例外ハンドリング。
- **データベースマイグレーション:** Flywayによるスキーマのバージョン管理。
- **型安全なデータベースアクセス:** jOOQによる型安全なSQLクエリ構築。

## 仕様と検証方法 (Specifications and Verification Methods)

本プロジェクトは、以下の要求仕様を満たしており、具体的なAPI操作またはテスト実行によってその動作を確認できます。

### 1. 技術要件

- **言語:** Kotlin
  - **説明:** プロジェクトの主要言語としてKotlin 1.9.25を使用しています。
  - **確認方法:** ソースコード（`src/main/kotlin`以下）を確認してください。
- **フレームワーク:** Spring Boot、jOOQ
  - **説明:** WebフレームワークにSpring Boot 3.5.9、型安全なデータベースアクセスにjOOQ 3.19.29を採用しています。
  - **確認方法:** `build.gradle`ファイルで依存関係を確認してください。また、APIエンドポイントの動作確認やテスト実行によって、Spring BootとjOOQが機能していることを検証できます。

### 2. 必要な機能

- **書籍と著者の情報をRDBに登録・更新できる機能**
  - **説明:** 著者と書籍のCRUD操作を提供し、データはPostgreSQLデータベースに永続化されます。
  - **確認方法:**
    - **登録:**
      - **著者:** `POST /authors` (リクエストボディ例参照)
      - **書籍:** `POST /books` (リクエストボディ例参照)
    - **更新:**
      - 著者: `PUT /authors/{id}` (リクエストボディ例参照)
      - 書籍: `PUT /books/{id}` (リクエストボディ例参照)
        - これらの操作は「APIエンドポイント」セクションのリクエスト/レスポンス例に沿って実行できます。
- **著者に紐づく本を取得できる機能**
  - **説明:** 特定の著者IDを指定して、その著者が執筆した書籍の一覧を取得できます。
  - **確認方法:**
    - **API:** `GET /authors/{id}/books` を実行してください。（「APIエンドポイント - 著者」セクション参照）

### 3. 書籍の属性

- **タイトル**
  - **説明:** 書籍にはタイトルがあり、登録・更新時に指定します。
  - **確認方法:** 書籍の登録 (`POST /books`) または取得 (`GET /books/{id}`) のレスポンスに含まれる`title`フィールドを確認してください。
- **価格（0以上であること）**
  - **説明:** 書籍の価格は0以上の整数である必要があります。
  - **確認方法:**
    - **成功例:** `POST /books` または `PUT /books/{id}` で`"price": 1000`のように0以上の値を指定し、正常に登録・更新されることを確認してください。
    - **失敗例:** `"price": -100`のように負の値を指定すると、`400 Bad Request`エラーが返されます。
- **著者（最低1人の著者を持つ。複数の著者を持つことが可能）**
  - **説明:** 書籍には最低1名の著者が必須であり、複数の著者を関連付けることができます。
  - **確認方法:**
    - **成功例:** `POST /books` または `PUT /books/{id}` で`"authorIds": [1, 2]`のように1名以上の著者IDを指定し、正常に登録・更新されることを確認してください。
    - **失敗例:** `"authorIds": []`のように著者IDを空にした場合、`400 Bad Request`エラーが返されます。
- **出版状況（未出版、出版済み。出版済みステータスのものを未出版には変更できない）**
  - **説明:** 書籍には出版状況（`PUBLISHED`または`UNPUBLISHED`）があり、一度`PUBLISHED`になると`UNPUBLISHED`には変更できません。
  - **確認方法:**
    - **成功例:**
      1. `POST /books` で`"publicationStatus": "UNPUBLISHED"`の書籍を登録します。
      2. `PUT /books/{id}` で`"publicationStatus": "PUBLISHED"`に更新します。
    - **失敗例:**
      1. `PUBLISHED`状態の書籍に対して、`PUT /books/{id}` で`"publicationStatus": "UNPUBLISHED"`に更新しようとすると、`400 Bad Request`エラーが返されます。

### 4. 著者の属性

- **名前**
  - **説明:** 著者には名前があり、登録・更新時に指定します。
  - **確認方法:** 著者の登録 (`POST /authors`) または取得 (`GET /authors/{id}`) のレスポンスに含まれる`name`フィールドを確認してください。
- **生年月日（現在日以前であること）**
  - **説明:** 著者の生年月日は現在日以前である必要があります。
  - **確認方法:**
    - **成功例:** `POST /authors` または `PUT /authors/{id}` で`"birthDate": "1990-01-01"`のように現在日以前の日付を指定し、正常に登録・更新されることを確認してください。
    - **失敗例:** `POST /authors` または `PUT /authors/{id}` で`"birthDate": "2999-01-01"`のように未来の日付を指定すると、`400 Bad Request`エラーが返されます。
- **著者も複数の書籍を執筆できる**
  - **説明:** 一人の著者が複数の書籍を執筆し、それらを取得できます。
  - **確認方法:** 複数の書籍を同じ著者IDで登録し (`POST /books`を複数回実行)、その後 `GET /authors/{id}/books` を実行して、その著者が執筆したすべての書籍が返されることを確認してください。

## 使用技術

- **言語:** Kotlin 1.9.25
- **フレームワーク:** Spring Boot 3.5.9
- **ビルドツール:** Gradle
- **データベース:** PostgreSQL 17 (ローカル開発用Docker Compose経由)
- **ORM/DAO:** jOOQ 3.19.29
- **データベースマイグレーション:** Flyway 10.10.0
- **テスト:** JUnit 5, MockK, SpringMockK
- **コード品質:** Spotless (Ktlint使用)
- **ランタイム:** JVM (Java 21)

## セットアップとローカル開発

### 前提条件

- JDK 21
- DockerとDocker Compose
- Gradle

### 1. リポジトリのクローン

```bash
git clone https://github.com/tadashi1105/book-api.git
cd book-api
```

### 2. Docker ComposeでPostgreSQLを起動

このプロジェクトには、PostgreSQLデータベースを簡単にセットアップするための`compose.yaml`ファイルが含まれています。

```bash
docker compose up -d
```

### 3. Flywayマイグレーションの実行 (オプション、Spring Boot起動時に自動実行されます)

`spring.flyway.enabled=true`（デフォルト）の場合、Spring Bootは起動時に自動的にFlywayマイグレーションを実行します。手動で実行することもできます。

```bash
./gradlew flywayMigrate
```

### 4. jOOQコードの生成 (オプション、Spring Bootのビルドプロセスで自動実行されます)

jOOQコードはビルドプロセス中に自動的に生成されます。手動で生成する必要がある場合：

```bash
./gradlew generateJooq
```

### 5. アプリケーションのビルドと実行

```bash
./gradlew bootRun
```

APIは`http://localhost:8080`で利用可能になります。

## APIエンドポイント

（アプリケーションが`http://localhost:8080`で実行されていると仮定します）

### 著者

- **`POST /authors`**
  - **説明:** 新しい著者を作成します。
  - **リクエストボディ例:**

    ```json
    {
        "name": "Jane Doe",
        "birthDate": "1990-01-01"
    }
    ```

  - **レスポンス例:** 生成されたIDを含む`AuthorResponse`オブジェクト。
- **`GET /authors/{id}`**
  - **説明:** IDで著者を取得します。
  - **レスポンス例:** `AuthorResponse`オブジェクト。
- **`GET /authors`**
  - **説明:** 全著者を取得します。
  - **レスポンス例:** `AuthorResponse`オブジェクトのリスト。
- **`PUT /authors/{id}`**
  - **説明:** 既存の著者を更新します。
  - **リクエストボディ例:**

    ```json
    {
        "name": "Jane D. Smith",
        "birthDate": "1990-01-01"
    }
    ```

  - **レスポンス例:** 更新された`AuthorResponse`オブジェクト。
- **`GET /authors/{id}/books`**
  - **説明:** 特定の著者に関連する全書籍を取得します。
  - **レスポンス例:** `BookResponse`オブジェクトのリスト。

### 書籍

- **`POST /books`**
  - **説明:** 新しい書籍を作成し、著者を関連付けます。
  - **リクエストボディ例:**

    ```json
    {
        "title": "The Kotlin Guide",
        "price": 2999,
        "authorIds": [1, 2],
        "publicationStatus": "PUBLISHED"
    }
    ```

  - **レスポンス例:** 生成されたIDと関連付けられた著者を含む`BookResponse`オブジェクト。
- **`GET /books/{id}`**
  - **説明:** IDで書籍を取得します。関連する著者も含まれます。
  - **レスポンス例:** `BookResponse`オブジェクト。
    - **`PUT /books/{id}`**
    - **説明:** 既存の書籍の詳細または著者を更新します。
  - **リクエストボディ例:**

    ```json
    {
        "title": "The Advanced Kotlin Guide",
        "price": 3499,
        "authorIds": [1],
        "publicationStatus": "UNPUBLISHED"
    }
    ```

  - **レスポンス例:** 更新された`BookResponse`オブジェクト。

## テストの実行

```bash
./gradlew test
```
