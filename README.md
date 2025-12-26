# 弁当予約管理アプリ

会社で使用する弁当の予約と管理を行うWebアプリケーション（MVP版）

## 概要

社員が自分で弁当を注文し、管理者の集計業務を自動化することで、業務効率を大幅に改善するアプリケーションです。

詳細な要件定義は `docs/要件定義書.md` を参照してください。

## 技術スタック

### バックエンド
- Kotlin 1.9.25
- Spring Boot 3.5.9
- Java 21
- Gradle (Kotlin DSL)
- Spring Data JPA (Hibernate)
- H2 Database (ファイルベース)

### フロントエンド
- Thymeleaf
- Bootstrap 5
- TypeScript
- FullCalendar

## 前提条件

- Java 21以降がインストールされていること
- IntelliJ IDEA または他のKotlin対応IDE（推奨）

## セットアップ方法

### 1. プロジェクトのクローン/ダウンロード

```bash
git clone <repository-url>
cd BentouReservation
```

### 2. 依存関係のインストール

```bash
./gradlew build
```

Windowsの場合:
```cmd
gradlew.bat build
```

### 3. アプリケーションの起動

```bash
./gradlew bootRun
```

Windowsの場合:
```cmd
gradlew.bat bootRun
```

### 4. アクセス

- アプリケーション: http://localhost:8080
- H2コンソール: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/bentou_reservation`
  - Username: `sa`
  - Password: (空白)

## プロジェクト構成

```
BentouReservation/
├── docs/                           # ドキュメント
│   └── 要件定義書.md
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/
│   │   │       └── bentou/        # Kotlinソースコード
│   │   └── resources/
│   │       ├── static/            # 静的ファイル
│   │       │   ├── css/          # スタイルシート
│   │       │   ├── js/           # コンパイル済みJavaScript
│   │       │   └── ts/           # TypeScriptソース
│   │       ├── templates/         # Thymeleafテンプレート
│   │       └── application.properties
│   └── test/
│       └── kotlin/
│           └── com/
│               └── bentou/        # テストコード
├── build.gradle.kts               # Gradleビルド設定
├── CLAUDE.md                      # プロジェクト概要
└── README.md                      # このファイル
```

## 開発

### H2コンソールの使用

開発中は、H2コンソールを使用してデータベースの内容を確認できます。

1. アプリケーションを起動
2. http://localhost:8080/h2-console にアクセス
3. JDBC URL: `jdbc:h2:file:./data/bentou_reservation` を入力
4. 接続

### ホットリロード

Spring Boot DevToolsを使用しているため、ファイルの変更時に自動的に再起動されます。

### TypeScriptのコンパイル

（今後実装予定）

## データベース

### データベースファイル

H2データベースはファイルベースで、`./data/` ディレクトリに保存されます。
このディレクトリは `.gitignore` に含まれています。

### DDL自動生成

`spring.jpa.hibernate.ddl-auto=update` により、エンティティの変更時に自動的にテーブルが更新されます。

## 今後の予定

- エンティティクラスの作成
- データベース設計の詳細化
- 画面設計・UI設計
- 基本機能の実装

詳細は `CLAUDE.md` の「開発進捗」セクションを参照してください。

## ライセンス

このプロジェクトは社内プロトタイプとして開発されています。
