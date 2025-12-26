/**
 * 弁当予約管理システム - 共通TypeScript
 */

/**
 * 削除確認ダイアログを表示
 */
function confirmDelete(message: string = '本当に削除しますか？'): boolean {
    return confirm(message);
}

/**
 * フォームのバリデーション
 */
function validateForm(formId: string): boolean {
    const form = document.getElementById(formId) as HTMLFormElement;
    if (!form) {
        return false;
    }
    return form.checkValidity();
}

/**
 * ローディング表示
 */
function showLoading(): void {
    const loading = document.querySelector('.loading');
    if (loading) {
        loading.classList.add('show');
    }
}

/**
 * ローディング非表示
 */
function hideLoading(): void {
    const loading = document.querySelector('.loading');
    if (loading) {
        loading.classList.remove('show');
    }
}

/**
 * 日付フォーマット (yyyy/MM/dd)
 */
function formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}/${month}/${day}`;
}

/**
 * 時刻フォーマット (HH:mm)
 */
function formatTime(date: Date): string {
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
}

/**
 * 現在時刻が締切時刻を過ぎているかチェック
 */
function isAfterDeadline(deadlineHour: number = 8, deadlineMinute: number = 30): boolean {
    const now = new Date();
    const deadline = new Date();
    deadline.setHours(deadlineHour, deadlineMinute, 0, 0);
    return now > deadline;
}

/**
 * アラートメッセージを表示
 */
function showAlert(message: string, type: 'success' | 'danger' | 'warning' | 'info' = 'info'): void {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.setAttribute('role', 'alert');

    const icon = getAlertIcon(type);
    alertDiv.innerHTML = `
        <i class="bi bi-${icon} me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    const container = document.querySelector('.container');
    if (container) {
        container.insertBefore(alertDiv, container.firstChild);
        // 5秒後に自動削除
        setTimeout(() => {
            alertDiv.remove();
        }, 5000);
    }
}

/**
 * アラートタイプに応じたアイコンを取得
 */
function getAlertIcon(type: string): string {
    switch (type) {
        case 'success':
            return 'check-circle-fill';
        case 'danger':
            return 'exclamation-triangle-fill';
        case 'warning':
            return 'exclamation-circle-fill';
        case 'info':
        default:
            return 'info-circle-fill';
    }
}

/**
 * 数値を通貨形式でフォーマット
 */
function formatCurrency(value: number): string {
    return `¥${value.toLocaleString('ja-JP')}`;
}

/**
 * ページ読み込み完了時の処理
 */
document.addEventListener('DOMContentLoaded', () => {
    // Bootstrap tooltipの有効化
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new (window as any).bootstrap.Tooltip(tooltipTriggerEl);
    });

    // フォームの削除ボタンに確認ダイアログを追加
    const deleteForms = document.querySelectorAll('form[action*="/delete"]');
    deleteForms.forEach((form) => {
        form.addEventListener('submit', (event) => {
            if (!confirmDelete()) {
                event.preventDefault();
            }
        });
    });

    // アラートの自動非表示
    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    alerts.forEach((alert) => {
        setTimeout(() => {
            const bsAlert = new (window as any).bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
});

// グローバルに公開
(window as any).confirmDelete = confirmDelete;
(window as any).validateForm = validateForm;
(window as any).showLoading = showLoading;
(window as any).hideLoading = hideLoading;
(window as any).formatDate = formatDate;
(window as any).formatTime = formatTime;
(window as any).isAfterDeadline = isAfterDeadline;
(window as any).showAlert = showAlert;
(window as any).formatCurrency = formatCurrency;
