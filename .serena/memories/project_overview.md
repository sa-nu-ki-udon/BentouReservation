# 弁当予約管理アプリ プロジェクト概要

## プロジェクト情報
- **プロジェクト名**: BentouReservation
- **目的**: 会社の弁当予約・管理を行うWebアプリケーション
- **ターゲットユーザー**: 一般社員（50名）、弁当管理者（3名）、経理担当（3名）
- **開発段階**: MVP版（ログイン機能・権限管理は後の追加機能）

## 技術スタック
- **言語**: Kotlin 1.9.25
- **フレームワーク**: Spring Boot 3.5.9
- **Java**: Java 21
- **ビルドツール**: Gradle（Kotlin DSL）
- **ORM**: Spring Data JPA (Hibernate)
- **DB**: H2 Database (ファイルベース)
- **テンプレート**: Thymeleaf
- **CSS**: Bootstrap 5
- **フロントエンド**: TypeScript
- **パッケージ**: Sample.BentouReservation

## ディレクトリ構造
```
src/main/kotlin/Sample/BentouReservation/
  ├── controller/       （10クラス）
  ├── service/          （9クラス）
  ├── repository/       （8インターフェース）
  ├── entity/           （9クラス）
  └── dto/              （8ファイル）

src/main/resources/
  ├── templates/
  │   ├── layout/       （共通レイアウト）
  │   ├── fragments/    （ヘッダー、ナビ、フッター）
  │   ├── vendors/      （業者管理）
  │   └── orders/       （注文系）
  └── static/
      ├── ts/           （TypeScript）
      └── js/           （コンパイル済みJS）
```

## 主要機能
1. **マスタ管理**: 業者、メニュー、メニューセット、社員、締め日
2. **カレンダー管理**: 日付ごとのメニュー設定、休日設定
3. **注文機能**: カレンダー形式での注文入力・変更（当日8:30までに変更可）
4. **日次集計**: 毎日8:45までに業者別自動集計
5. **月次集計**: 個人別・業者別の請求額集計

## データベース設計
8テーブル構成：
1. vendors (業者)
2. menus (メニュー)
3. menu_sets (メニューセット)
4. menu_set_items (セット詳細)
5. calendar_settings (カレンダー設定)
6. employees (社員)
7. orders (注文)
8. billing_closing_dates (締め日)

## 現在の状況
- ✅ バックエンド層完成（Entity, Repository, Service, Controller）
- ✅ フロントエンド基盤作成（レイアウト、フラグメント）
- 📝 テンプレート作成進行中（7/35個作成）
  - 作成済み: index.html, vendors/list.html, orders/select-employee.html
  - 作成予定: 各CRUD画面、集計画面、カレンダー画面

## テンプレート一覧ファイル
コントローラーの存在状況に基づいて、作成すべきテンプレートが記載されている：
- 優先順位: 注文系 > カレンダー設定 > 日次集計 > 月次集計 > マスタ管理
