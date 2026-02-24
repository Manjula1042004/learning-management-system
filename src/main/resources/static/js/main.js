// Main JavaScript for LMS Platform

document.addEventListener('DOMContentLoaded', function() {
    initializeDropdowns();
    initializeFlashMessages();
    initializeForms();
    initializeVideoPlayers();
});

// Dropdown functionality
function initializeDropdowns() {
    document.addEventListener('click', function(e) {
        const dropdowns = document.querySelectorAll('.group');
        dropdowns.forEach(dropdown => {
            if (!dropdown.contains(e.target)) {
                const menu = dropdown.querySelector('.hidden');
                if (menu) menu.classList.add('hidden');
            }
        });
    });

    document.querySelectorAll('.group').forEach(group => {
        const button = group.querySelector('button');
        if (button) {
            button.addEventListener('click', function(e) {
                e.stopPropagation();
                const menu = this.parentNode.querySelector('.hidden');
                if (menu) {
                    menu.classList.toggle('hidden');
                }
            });
        }
    });
}

// Auto-hide flash messages
function initializeFlashMessages() {
    const flashMessages = document.querySelectorAll('.bg-green-100, .bg-red-100, .bg-blue-100');
    flashMessages.forEach(message => {
        setTimeout(() => {
            message.style.transition = 'opacity 0.5s ease';
            message.style.opacity = '0';
            setTimeout(() => {
                if (message.parentNode) {
                    message.parentNode.removeChild(message);
                }
            }, 500);
        }, 5000);
    });
}

// Form enhancements
function initializeForms() {
    // Password strength indicator
    const passwordInputs = document.querySelectorAll('input[type="password"]');
    passwordInputs.forEach(input => {
        input.addEventListener('input', function() {
            const strength = calculatePasswordStrength(this.value);
            updatePasswordStrengthIndicator(this, strength);
        });
    });

    // File upload preview
    const fileInputs = document.querySelectorAll('input[type="file"]');
    fileInputs.forEach(input => {
        input.addEventListener('change', function() {
            const fileName = this.files[0]?.name;
            if (fileName) {
                let label = this.nextElementSibling;
                if (!label || !label.classList.contains('file-name')) {
                    label = document.createElement('span');
                    label.className = 'file-name text-sm text-gray-600 ml-2';
                    this.parentNode.appendChild(label);
                }
                label.textContent = fileName;
            }
        });
    });
}

// Video player initialization
function initializeVideoPlayers() {
    const videoPlayers = document.querySelectorAll('video');
    videoPlayers.forEach(video => {
        video.addEventListener('timeupdate', function() {
            const progress = (this.currentTime / this.duration) * 100;
            updateProgressBar(this, progress);
        });
    });
}

function updateProgressBar(videoElement, progress) {
    const progressBar = videoElement.parentNode.querySelector('.progress-bar');
    if (progressBar) {
        progressBar.style.width = progress + '%';
    }
}

// Password strength calculator
function calculatePasswordStrength(password) {
    if (!password) return 0;

    let strength = 0;
    if (password.length >= 8) strength++;
    if (password.match(/[a-z]/) && password.match(/[A-Z]/)) strength++;
    if (password.match(/\d/)) strength++;
    if (password.match(/[^a-zA-Z\d]/)) strength++;
    return strength;
}

function updatePasswordStrengthIndicator(input, strength) {
    let indicator = input.parentNode.querySelector('.password-strength');
    if (!indicator) {
        indicator = document.createElement('div');
        indicator.className = 'password-strength mt-1';
        input.parentNode.appendChild(indicator);
    }

    const colors = ['bg-red-500', 'bg-orange-500', 'bg-yellow-500', 'bg-green-500'];
    const texts = ['Very Weak', 'Weak', 'Medium', 'Strong'];

    indicator.innerHTML = `
        <div class="flex items-center space-x-2">
            <div class="flex-1 bg-gray-200 rounded-full h-2">
                <div class="h-2 rounded-full transition-all duration-300 ${colors[strength - 1] || 'bg-red-500'}"
                     style="width: ${(strength / 4) * 100}%"></div>
            </div>
            <span class="text-xs text-gray-600">${texts[strength - 1] || 'Very Weak'}</span>
        </div>
    `;
}

// Utility functions
function formatDuration(seconds) {
    if (!seconds) return '0:00';

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);

    if (hours > 0) {
        return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// API helper functions
async function apiCall(url, options = {}) {
    try {
        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
}

// Export for use in other modules
window.LMS = {
    initializeVideoPlayer: initializeVideoPlayers,
    formatDuration,
    debounce,
    apiCall
};

// Initialize everything when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeDropdowns);
} else {
    initializeDropdowns();
}