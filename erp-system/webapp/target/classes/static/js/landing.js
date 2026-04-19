document.addEventListener('DOMContentLoaded', () => {
    initScrollReveal();
    initHoverGlow();
    initIsometricScroll();
    initWidgets();
    initCanvas();
});

function initScrollReveal() {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('is-visible');
            }
        });
    }, { threshold: 0.1, rootMargin: "0px 0px -50px 0px" });

    document.querySelectorAll('.reveal-element').forEach(el => observer.observe(el));
}

function initHoverGlow() {
    const cards = document.querySelectorAll('.hover-glow');
    
    cards.forEach(card => {
        card.addEventListener('mousemove', e => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            card.style.setProperty('--mouse-x', `${x}px`);
            card.style.setProperty('--mouse-y', `${y}px`);

            // Add 3D Tilt effect
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            const rotateX = ((y - centerY) / centerY) * -5; // max 5 deg
            const rotateY = ((x - centerX) / centerX) * 5;  // max 5 deg
            card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.02, 1.02, 1.02)`;
        });

        card.addEventListener('mouseleave', () => {
            card.style.transform = `perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)`;
        });
    });
}

let scrollYTarget = 0;
let scrollYCurrent = 0;

function initIsometricScroll() {
    const stage = document.getElementById('iso-stage');
    if (!stage) return;
    
    const layer2 = document.querySelector('.iso-layer-2');
    const layer3 = document.querySelector('.iso-layer-3');
    const container = document.querySelector('.iso-grid-container');
    
    window.addEventListener('scroll', () => {
        scrollYTarget = window.scrollY;
    });

    function renderLoop() {
        // LERP for buttery smooth scroll interpolation
        scrollYCurrent += (scrollYTarget - scrollYCurrent) * 0.08;
        
        const maxScroll = 500;
        let progress = Math.min(scrollYCurrent / maxScroll, 1);
        const easeOut = Math.sin((progress * Math.PI) / 2);
        
        const dist2 = 100 - (easeOut * 100); 
        const dist3 = 200 - (easeOut * 200);
        
        if (layer2) layer2.style.transform = `translateZ(${dist2}px)`;
        if (layer3) layer3.style.transform = `translateZ(${dist3}px)`;
        
        if (container) {
            const rotX = 60 - (easeOut * 12);
            const rotZ = -45 + (easeOut * 8);
            container.style.transform = `rotateX(${rotX}deg) rotateZ(${rotZ}deg)`;
        }
        
        requestAnimationFrame(renderLoop);
    }
    
    renderLoop();
}

function initWidgets() {
    const sortLines = document.querySelectorAll('.s-line');
    if (sortLines.length > 0) {
        setInterval(() => {
            sortLines.forEach(line => line.classList.remove('active'));
            const randomIdx = Math.floor(Math.random() * sortLines.length);
            sortLines[randomIdx].classList.add('active');
            
            sortLines.forEach(line => {
                const currentHeight = parseInt(line.style.height) || 50;
                let newHeight = currentHeight + (Math.random() * 20 - 10);
                if (newHeight > 100) newHeight = 100;
                if (newHeight < 10) newHeight = 10;
                line.style.height = `${newHeight}%`;
            });
        }, 800);
    }
}

function initCanvas() {
    const canvas = document.getElementById('heroCanvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    let width, height;
    let particles = [];

    function resize() {
        width = canvas.width = window.innerWidth;
        height = canvas.height = window.innerHeight;
    }

    class Particle {
        constructor() {
            this.x = Math.random() * width;
            this.y = Math.random() * height;
            this.vx = (Math.random() - 0.5) * 0.5;
            this.vy = (Math.random() - 0.5) * 0.5;
            this.radius = Math.random() * 1.5;
        }
        update() {
            this.x += this.vx;
            this.y += this.vy;

            if (this.x < 0) this.x = width;
            if (this.x > width) this.x = 0;
            if (this.y < 0) this.y = height;
            if (this.y > height) this.y = 0;
        }
        draw() {
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
            ctx.fillStyle = 'rgba(100, 150, 255, 0.4)';
            ctx.fill();
        }
    }

    function init() {
        resize();
        particles = [];
        const count = Math.min(window.innerWidth / 10, 100);
        for (let i = 0; i < count; i++) {
            particles.push(new Particle());
        }
    }

    function animate() {
        ctx.clearRect(0, 0, width, height);
        particles.forEach(p => {
            p.update();
            p.draw();
        });
        requestAnimationFrame(animate);
    }

    window.addEventListener('resize', init);
    init();
    animate();
}
