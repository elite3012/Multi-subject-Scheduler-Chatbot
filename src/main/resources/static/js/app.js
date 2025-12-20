// filepath: src/main/resources/static/ui/app.js
const API_BASE = '/api/chatbot';

// State
let currentPlan = null;
let messageHistory = [];

// DOM Elements
const messagesContainer = document.getElementById('messagesContainer');
const commandInput = document.getElementById('commandInput');
const sendBtn = document.getElementById('sendBtn');
const clearChatBtn = document.getElementById('clearChat');
const planStatus = document.getElementById('planStatus');
const quickCmds = document.querySelectorAll('.quick-cmd');
const themeToggle = document.getElementById('themeToggle');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    initEventListeners();
    initParallax();
    initTheme();
    fetchCurrentPlan();
});

function initEventListeners() {
    sendBtn.addEventListener('click', sendCommand);
    commandInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendCommand();
    });
    clearChatBtn.addEventListener('click', clearChat);
    
    quickCmds.forEach(btn => {
        btn.addEventListener('click', () => {
            commandInput.value = btn.dataset.cmd;
            commandInput.focus();
        });
    });
}

function initParallax() {
    // Parallax disabled to prevent content overlap
    // Hero section now scrolls normally with page
}

// API Functions
async function sendCommand() {
    const command = commandInput.value.trim();
    if (!command) return;
    
    addMessage(command, 'user');
    commandInput.value = '';
    
    try {
        showTypingIndicator();
        
        const response = await fetch(`${API_BASE}/command`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ command })
        });
        
        const result = await response.json();
        removeTypingIndicator();
        
        if (result.success) {
            handleSuccessResponse(result);
        } else {
            addMessage(`❌ Error: ${result.message}`, 'bot');
        }
        
        await fetchCurrentPlan();
    } catch (error) {
        removeTypingIndicator();
        addMessage(`❌ Connection error: ${error.message}`, 'bot');
    }
}

function handleSuccessResponse(result) {
    let message = `✅ ${result.message}`;
    
    // Handle different command types
    if (result.commandHistory) {
        message += '\n\n📜 Command History:\n';
        result.commandHistory.forEach((entry, i) => {
            message += `\n${i + 1}. [${entry.formattedTimestamp}] ${entry.command}`;
        });
    }
    
    if (result.schedule) {
        message += '\n\n📅 Schedule Generated!\nUse "show schedule" to view details.';
    }
    
    if (result.updatedPlan && result.updatedPlan.courses) {
        const courses = result.updatedPlan.courses;
        message += `\n\n📚 Current Subjects: ${courses.length}`;
    }
    
    // Check if this is a clear command - reset UI
    if (result.message && result.message.toLowerCase().includes('cleared')) {
        currentPlan = null;
        updatePlanStatus();
    }
    
    addMessage(message, 'bot');
}

async function fetchCurrentPlan() {
    try {
        const response = await fetch(`${API_BASE}/plan`);
        if (!response.ok) {
            console.error('Failed to fetch plan:', response.status);
            return;
        }
        const text = await response.text();
        if (text && text.trim()) {
            currentPlan = JSON.parse(text);
        } else {
            currentPlan = null;
        }
        updatePlanStatus();
    } catch (error) {
        console.error('Failed to fetch plan:', error);
        currentPlan = null;
        updatePlanStatus();
    }
}

async function fetchSchedule() {
    try {
        const response = await fetch(`${API_BASE}/schedule`);
        const schedule = await response.text();
        addMessage(schedule, 'bot', true);
    } catch (error) {
        addMessage(`❌ Failed to fetch schedule: ${error.message}`, 'bot');
    }
}

// UI Functions
function addMessage(content, type, isFormatted = false) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}-message`;
    
    const contentDiv = document.createElement('div');
    contentDiv.className = 'message-content';
    
    if (isFormatted) {
        const pre = document.createElement('pre');
        pre.textContent = content;
        contentDiv.appendChild(pre);
    } else {
        // Split by newlines and create paragraphs
        content.split('\n').forEach(line => {
            if (line.trim()) {
                const p = document.createElement('p');
                p.textContent = line;
                contentDiv.appendChild(p);
            }
        });
    }
    
    messageDiv.appendChild(contentDiv);
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
    
    messageHistory.push({ content, type, timestamp: new Date() });
}

function showTypingIndicator() {
    const indicator = document.createElement('div');
    indicator.className = 'message bot-message typing-indicator';
    indicator.id = 'typingIndicator';
    indicator.innerHTML = '<div class="message-content"><p class="loading">Thinking</p></div>';
    messagesContainer.appendChild(indicator);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function removeTypingIndicator() {
    const indicator = document.getElementById('typingIndicator');
    if (indicator) indicator.remove();
}

function updatePlanStatus() {
    if (!currentPlan || !currentPlan.courses || currentPlan.courses.length === 0) {
        planStatus.innerHTML = '<p class="empty-state">No subjects added yet</p>';
        return;
    }
    
    let html = '<div style="font-size: 0.9rem;">';
    html += `<p style="margin-bottom: 0.5rem;"><strong>${currentPlan.courses.length} Subject(s)</strong></p>`;
    
    currentPlan.courses.forEach(course => {
        const priorityColor = {
            'HIGH': '🔴',
            'MEDIUM': '🟡',
            'LOW': '🟢'
        }[course.priority] || '⚪';
        
        html += `<p style="margin: 0.25rem 0;">${priorityColor} ${course.id} (${course.workloadHours}h)</p>`;
    });
    
    html += '</div>';
    planStatus.innerHTML = html;
}

function clearChat() {
    if (confirm('Clear all messages?')) {
        messagesContainer.innerHTML = `
            <div class="message bot-message">
                <div class="message-content">
                    <p>👋 Chat cleared! Ready for new commands.</p>
                </div>
            </div>
        `;
        messageHistory = [];
    }
}

// Auto-complete suggestions
const commandSuggestions = [
    'add subject "Math" hours 10 priority HIGH',
    'set availability on 2025-12-20 capacity 8 hours',
    'list subjects',
    'generate schedule',
    'show schedule',
    'show history',
    'clear all'
];

commandInput.addEventListener('input', (e) => {
    const value = e.target.value.toLowerCase();
    const suggestionsDiv = document.getElementById('suggestions');
    suggestionsDiv.innerHTML = '';
    
    if (value.length > 2) {
        const matches = commandSuggestions.filter(cmd => 
            cmd.toLowerCase().includes(value)
        ).slice(0, 3);
        
        matches.forEach(cmd => {
            const chip = document.createElement('div');
            chip.className = 'suggestion-chip';
            chip.textContent = cmd;
            chip.addEventListener('click', () => {
                commandInput.value = cmd;
                commandInput.focus();
                suggestionsDiv.innerHTML = '';
            });
            suggestionsDiv.appendChild(chip);
        });
    }
});

// Theme Management
function initTheme() {
    // Load saved theme or default to light
    const savedTheme = localStorage.getItem('theme') || 'light';
    setTheme(savedTheme);
    
    themeToggle.addEventListener('click', toggleTheme);
}

function setTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
    
    // Update icon
    const icon = themeToggle.querySelector('.theme-icon');
    icon.textContent = theme === 'dark' ? '☀️' : '🌙';
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
}

// Smooth scroll
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    });
});