/* ==========================================================================
   FoodExpress Main Client Script
   ========================================================================== */

document.addEventListener('DOMContentLoaded', function () {

    // 1. Coupon Copy Functionality
    const copyBtns = document.querySelectorAll('.btn-copy-coupon');
    copyBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            const code = this.getAttribute('data-code');
            if (code) {
                navigator.clipboard.writeText(code).then(() => {
                    const originalText = this.innerText;
                    this.innerText = 'Copied!';
                    this.classList.replace('btn-outline-food', 'btn-success');
                    setTimeout(() => {
                        this.innerText = originalText;
                        this.classList.replace('btn-success', 'btn-outline-food');
                    }, 2000);
                });
            }
        });
    });

    // 2. Auto-hide alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

    // 3. Image error fallback
    const images = document.querySelectorAll('img');
    images.forEach(img => {
        img.addEventListener('error', function () {
            this.src = 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500'; // high quality food fallback
        });
    });

});
