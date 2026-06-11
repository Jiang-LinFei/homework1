// 图书馆信息管理系统  前端通用脚本

// 删除/危险操作前的二次确认；返回布尔值供 onclick 使用
function confirmAction(message) {
    return window.confirm(message || '确定要执行该操作吗？');
}

// 借阅登记表单提交前的简单校验
function validateBorrow(form) {
    if (!form.bookId.value) { alert('请选择图书'); return false; }
    if (!form.readerId.value) { alert('请选择读者'); return false; }
    var days = parseInt(form.days.value, 10);
    if (isNaN(days) || days <= 0) { alert('借阅天数必须为正整数'); return false; }
    return true;
}
