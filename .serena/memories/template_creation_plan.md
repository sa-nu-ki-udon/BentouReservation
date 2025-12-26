# テンプレート作成計画

## 必要なテンプレート一覧（作成済み3個 + 作成必要36個 = 39個）

### 作成済み ✅
1. index.html
2. vendors/list.html
3. orders/select-employee.html

### 優先度1: 注文系（4個）- コア機能
- orders/calendar - 注文カレンダー表示
- orders/new - 注文入力画面
- orders/edit - 注文変更画面
- orders/history - 注文履歴画面

### 優先度2: カレンダー設定（3個）- コア機能
- calendar/index - カレンダー表示画面
- calendar/setting-form - 単日設定フォーム
- calendar/bulk-setting-form - 一括設定フォーム

### 優先度3: 日次集計（4個）- 業務に必須
- daily-aggregation/index - 日次集計画面
- daily-aggregation/select-vendor - 業者選択（印刷前）
- daily-aggregation/print-vendor - 業者別印刷画面
- daily-aggregation/print-all - 全業者印刷画面

### 優先度4: 月次集計（9個）
- monthly-billing/index - サマリー画面
- monthly-billing/select-employee - 社員選択画面
- monthly-billing/employee - 社員別集計
- monthly-billing/all-employees - 全社員集計一覧
- monthly-billing/select-vendor - 業者選択画面
- monthly-billing/vendor - 業者別集計
- monthly-billing/all-vendors - 全業者集計一覧
- monthly-billing/print-employee - 社員別集計印刷
- monthly-billing/print-vendor - 業者別集計印刷

### 優先度5: マスタ管理（16個）
#### 業者管理（4個）
- vendors/detail
- vendors/new
- vendors/edit

#### メニュー管理（5個）
- menus/list
- menus/detail
- menus/new
- menus/edit
- menus/change-price

#### メニューセット管理（4個）
- menu-sets/list
- menu-sets/detail
- menu-sets/new
- menu-sets/edit

#### 社員管理（4個）
- employees/list
- employees/detail
- employees/new
- employees/edit

#### 締め日設定（4個）
- billing-closing-dates/list
- billing-closing-dates/detail
- billing-closing-dates/new
- billing-closing-dates/edit

## テンプレート作成パターン
各テンプレートの基本構造は以下の通り：
- 共通レイアウト (layout/default.html) を継承
- 検索フォーム、テーブル、フォームなど共通パターンを使用
- Bootstrap 5 による統一されたスタイル
- エラーメッセージとフラッシュメッセージ対応

## 作成順序
優先度順で作成します。優先度1と2が完了したら、動作確認とテストデータ投入を実施します。
