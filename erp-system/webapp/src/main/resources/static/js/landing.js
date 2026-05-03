(function () {
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (prefersReducedMotion) {
        document.body.classList.add('reduced-motion');
    }

    document.addEventListener('DOMContentLoaded', () => {
        initReveal();
        initHoverGlow();
        initSceneStatus();
        initStoryTimeline();
        initTimetableScene();
    });

    function initReveal() {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    const delay = entry.target.getAttribute('data-delay');
                    if (delay) entry.target.style.setProperty('--delay', delay);
                    entry.target.classList.add('is-visible');
                }
            });
        }, { threshold: 0.16, rootMargin: '0px 0px -60px 0px' });

        document.querySelectorAll('.reveal-element').forEach((element) => observer.observe(element));
    }

    function initHoverGlow() {
        document.querySelectorAll('.hover-glow, .scene-status').forEach((card) => {
            card.addEventListener('mousemove', (event) => {
                const rect = card.getBoundingClientRect();
                const x = event.clientX - rect.left;
                const y = event.clientY - rect.top;
                card.style.setProperty('--mouse-x', `${x}px`);
                card.style.setProperty('--mouse-y', `${y}px`);

                if (prefersReducedMotion) return;

                const rotateX = ((y - rect.height / 2) / rect.height) * -7;
                const rotateY = ((x - rect.width / 2) / rect.width) * 7;
                card.style.transform = `perspective(900px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateZ(0)`;
            });

            card.addEventListener('mouseleave', () => {
                card.style.transform = 'perspective(900px) rotateX(0deg) rotateY(0deg) translateZ(0)';
            });
        });
    }

    function initSceneStatus() {
        const title = document.getElementById('sceneTitle');
        const details = document.getElementById('sceneDetails');
        const phase = document.getElementById('scenePhase');
        const conflicts = document.getElementById('metricConflicts');
        const rooms = document.getElementById('metricRooms');
        const load = document.getElementById('metricLoad');

        const steps = [
            ['Live Optimization', 'Command preview is warming up', 'Rooms, faculty, and subjects are being mapped into a single scheduling space.', '12', '24', '68%'],
            ['Constraint Intake', 'Every rule becomes visible', 'Availability windows and room capacities separate into readable layers.', '9', '24', '71%'],
            ['Relationship Map', 'The schedule field is connecting', 'Rooms, subjects, faculty, and sections begin linking into one readable system.', '7', '24', '73%'],
            ['Conflict Scan', 'Pressure points are highlighted', 'Overlaps glow before publication so planners can act early.', '5', '24', '76%'],
            ['Load Balance', 'The engine is distributing pressure', 'Faculty hours and room usage are balanced before the grid settles.', '1', '24', '80%'],
            ['Publish Ready', 'The final grid is stabilizing', 'Faculty load, classroom use, and section coverage settle into a practical timetable.', '0', '24', '82%'],
            ['Platform View', 'The control center is ready', 'Generate, inspect, adjust, and publish with confidence.', '0', '24', '82%'],
            ['Workflow Ready', 'Generate, inspect, balance, publish', 'The planning loop is arranged into four calm operational phases.', '0', '24', '82%'],
            ['Publication Ready', 'The timetable is ready to review', 'A clean schedule can now be inspected, adjusted, and shared.', '0', '24', '82%'],
            ['Dashboard Launch', 'Your command center is ready', 'Move from the visual story into the working application.', '0', '24', '82%']
        ];

        function applyStep(index) {
            const data = steps[Math.max(0, Math.min(steps.length - 1, index))];
            if (!data || !title || !details || !phase) return;
            phase.textContent = data[0];
            title.textContent = data[1];
            details.textContent = data[2];
            conflicts.textContent = data[3];
            rooms.textContent = data[4];
            load.textContent = data[5];
        }

        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    applyStep(Number(entry.target.getAttribute('data-scene-step') || 0));
                }
            });
        }, { threshold: 0.45 });

        document.querySelectorAll('[data-scene-step]').forEach((element) => observer.observe(element));
        window.updateSceneStatus = applyStep;
    }

    function initStoryTimeline() {
        const story = document.querySelector('.story');
        const phases = Array.from(document.querySelectorAll('.story-phase'));
        if (!story || phases.length === 0) return;

        let activeIndex = 0;
        let timelinePoints = [];
        let sweepTimer;
        let ticking = false;

        function measureTimeline() {
            const storyRect = story.getBoundingClientRect();
            timelinePoints = phases.map((phase) => {
                const dot = phase.querySelector('.phase-dot');
                const rect = dot.getBoundingClientRect();
                return {
                    phase,
                    x: rect.left + rect.width / 2 - storyRect.left,
                    y: rect.top + rect.height / 2 - storyRect.top,
                    viewportY: rect.top + rect.height / 2
                };
            });

            const first = timelinePoints[0];
            const last = timelinePoints[timelinePoints.length - 1];
            if (!first || !last) return;

            story.style.setProperty('--timeline-x', `${first.x}px`);
            story.style.setProperty('--timeline-start', `${first.y}px`);
            story.style.setProperty('--timeline-height', `${Math.max(1, last.y - first.y)}px`);
            updateProgress(false);
        }

        function updateProgress(animateSweep) {
            const first = timelinePoints[0];
            const current = timelinePoints[activeIndex];
            if (!first || !current) return;

            const progress = Math.max(0, current.y - first.y);
            story.style.setProperty('--timeline-progress', `${progress}px`);
            story.style.setProperty('--timeline-sweep', `${progress}px`);

            if (!animateSweep) return;

            story.classList.remove('is-traveling');
            void story.offsetWidth;
            story.classList.add('is-traveling');
            window.clearTimeout(sweepTimer);
            sweepTimer = window.setTimeout(() => story.classList.remove('is-traveling'), 780);
        }

        function setActive(nextIndex) {
            if (nextIndex === activeIndex) return;
            activeIndex = nextIndex;
            updateProgress(true);

            phases.forEach((phase, index) => {
                phase.classList.toggle('is-active', index === activeIndex);
                phase.classList.toggle('is-past', index < activeIndex);
            });
        }

        function syncActiveFromScroll() {
            ticking = false;
            if (timelinePoints.length !== phases.length) {
                measureTimeline();
            } else {
                const storyRect = story.getBoundingClientRect();
                timelinePoints.forEach((point) => {
                    const dot = point.phase.querySelector('.phase-dot');
                    const rect = dot.getBoundingClientRect();
                    point.x = rect.left + rect.width / 2 - storyRect.left;
                    point.y = rect.top + rect.height / 2 - storyRect.top;
                    point.viewportY = rect.top + rect.height / 2;
                });
            }

            const focusY = window.innerHeight * 0.46;
            let nextIndex = activeIndex;
            let minDistance = Infinity;
            timelinePoints.forEach((point, index) => {
                const distance = Math.abs(point.viewportY - focusY);
                if (distance < minDistance) {
                    minDistance = distance;
                    nextIndex = index;
                }
            });
            if (nextIndex === activeIndex) {
                updateProgress(false);
                return;
            }
            setActive(nextIndex);
        }

        function requestSync() {
            if (ticking) return;
            ticking = true;
            requestAnimationFrame(syncActiveFromScroll);
        }

        phases.forEach((phase, index) => {
            phase.classList.toggle('is-active', index === 0);
            phase.classList.toggle('is-past', false);
        });

        window.addEventListener('resize', measureTimeline);
        window.addEventListener('scroll', requestSync, { passive: true });
        window.addEventListener('load', measureTimeline);
        measureTimeline();
        requestSync();
    }

    function hasWebGL() {
        try {
            const canvas = document.createElement('canvas');
            return Boolean(window.WebGLRenderingContext && (canvas.getContext('webgl') || canvas.getContext('experimental-webgl')));
        } catch (error) {
            return false;
        }
    }

    function initCanvasFallback(canvas) {
        const ctx = canvas.getContext('2d');
        if (!ctx) return;

        const subjects = ['PHY', 'MATH', 'CS', 'ENG', 'CHEM', 'BIO', 'AI', 'DSA', 'LAB', 'ECO', 'OS', 'DBMS'];
        const nodes = Array.from({ length: 28 }, (_, index) => ({
            x: Math.random(),
            y: Math.random(),
            phase: index * 0.43,
            conflict: index % 7 === 0
        }));
        let pointerX = 0;
        let pointerY = 0;
        let scrollProgress = 0;
        let start = performance.now();

        function resizeFallback() {
            const dpr = Math.max(1, Math.min(2, window.devicePixelRatio || 1));
            canvas.width = Math.floor(window.innerWidth * dpr);
            canvas.height = Math.floor(window.innerHeight * dpr);
            canvas.style.width = `${window.innerWidth}px`;
            canvas.style.height = `${window.innerHeight}px`;
            ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        }

        function updateFallbackScroll() {
            const max = document.documentElement.scrollHeight - window.innerHeight;
            scrollProgress = max > 0 ? window.scrollY / max : 0;
        }

        function drawRoundedRect(x, y, width, height, radius) {
            ctx.beginPath();
            ctx.moveTo(x + radius, y);
            ctx.lineTo(x + width - radius, y);
            ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
            ctx.lineTo(x + width, y + height - radius);
            ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
            ctx.lineTo(x + radius, y + height);
            ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
            ctx.lineTo(x, y + radius);
            ctx.quadraticCurveTo(x, y, x + radius, y);
            ctx.closePath();
        }

        function drawScene(now) {
            const width = window.innerWidth;
            const height = window.innerHeight;
            const t = (now - start) / 1000;
            const driftX = Math.sin(t * 0.26) * 22 + pointerX * 18;
            const driftY = Math.cos(t * 0.22) * 14 + pointerY * 14;
            const gridX = width * 0.62 + driftX - scrollProgress * width * 0.18;
            const gridY = height * 0.46 + driftY - scrollProgress * 80;
            const tilt = -0.28 + Math.sin(t * 0.18) * 0.04 + pointerX * 0.08;
            const cellW = Math.max(58, Math.min(92, width * 0.062));
            const cellH = Math.max(34, Math.min(52, height * 0.055));

            ctx.clearRect(0, 0, width, height);
            ctx.save();
            ctx.fillStyle = '#060b12';
            ctx.fillRect(0, 0, width, height);

            const glow = ctx.createRadialGradient(width * 0.72, height * 0.34, 0, width * 0.72, height * 0.34, width * 0.46);
            glow.addColorStop(0, 'rgba(132,220,198,0.20)');
            glow.addColorStop(0.42, 'rgba(125,183,255,0.10)');
            glow.addColorStop(1, 'rgba(6,11,18,0)');
            ctx.fillStyle = glow;
            ctx.fillRect(0, 0, width, height);

            nodes.forEach((node, index) => {
                const x = node.x * width + Math.sin(t * 0.6 + node.phase) * 24;
                const y = node.y * height + Math.cos(t * 0.5 + node.phase) * 20;
                const pulse = node.conflict ? (Math.sin(t * 4 + index) + 1) * 0.5 : (Math.sin(t * 1.8 + index) + 1) * 0.5;
                ctx.beginPath();
                ctx.arc(x, y, node.conflict ? 3.5 + pulse * 4 : 2 + pulse * 1.2, 0, Math.PI * 2);
                ctx.fillStyle = node.conflict && scrollProgress < 0.55
                    ? `rgba(255,159,139,${0.22 + pulse * 0.34})`
                    : `rgba(132,220,198,${0.10 + pulse * 0.18})`;
                ctx.shadowBlur = node.conflict ? 22 : 10;
                ctx.shadowColor = node.conflict ? 'rgba(255,159,139,0.58)' : 'rgba(132,220,198,0.32)';
                ctx.fill();
                ctx.shadowBlur = 0;
            });

            ctx.save();
            ctx.translate(gridX, gridY);
            ctx.rotate(tilt);
            ctx.transform(1, -0.18, 0.16, 0.86, 0, 0);

            for (let row = 0; row < 4; row += 1) {
                for (let col = 0; col < 6; col += 1) {
                    const x = (col - 3) * cellW;
                    const y = (row - 2) * cellH;
                    const breathe = Math.sin(t * 1.6 + row * 0.8 + col * 0.4) * 3;
                    const label = subjects[(row * 6 + col) % subjects.length];
                    const time = `${9 + col}${col < 3 ? 'A' : 'P'}`;
                    ctx.save();
                    ctx.translate(x + Math.sin(t * 0.8 + col) * 5, y + breathe);
                    drawRoundedRect(0, 0, cellW * 0.82, cellH * 0.74, 7);
                    ctx.fillStyle = `rgba(${col % 2 ? '125,183,255' : '132,220,198'},0.58)`;
                    ctx.fill();
                    ctx.strokeStyle = 'rgba(247,251,250,0.34)';
                    ctx.lineWidth = 1;
                    ctx.stroke();
                    ctx.fillStyle = '#f7fbfa';
                    ctx.font = '800 12px Inter, Arial, sans-serif';
                    ctx.textAlign = 'center';
                    ctx.fillText(label, cellW * 0.41, cellH * 0.31);
                    ctx.fillStyle = 'rgba(247,251,250,0.72)';
                    ctx.font = '700 9px Inter, Arial, sans-serif';
                    ctx.fillText(time, cellW * 0.41, cellH * 0.55);
                    ctx.restore();
                }
            }

            const cards = [
                ['Physics', 'Room 301', '9:00 AM', false],
                ['Math', 'Room 201', '10:00 AM', false],
                ['English', 'Room 118', '11:00 AM', scrollProgress < 0.48]
            ];
            cards.forEach((card, index) => {
                const x = (index - 1.45) * cellW * 1.65;
                const y = -cellH * 2.65 + Math.sin(t * 1.1 + index) * 5;
                drawRoundedRect(x, y, cellW * 1.28, cellH * 0.86, 8);
                ctx.fillStyle = card[3] ? 'rgba(72,22,22,0.76)' : 'rgba(7,17,24,0.76)';
                ctx.fill();
                ctx.strokeStyle = card[3] ? 'rgba(255,159,139,0.84)' : 'rgba(132,220,198,0.62)';
                ctx.stroke();
                ctx.fillStyle = '#f7fbfa';
                ctx.font = '800 12px Inter, Arial, sans-serif';
                ctx.fillText(card[0], x + cellW * 0.64, y + 17);
                ctx.fillStyle = card[3] ? '#ffb5a5' : '#c8fff2';
                ctx.font = '700 9px Inter, Arial, sans-serif';
                ctx.fillText(card[3] ? `${card[2]} CONFLICT` : `${card[1]} ${card[2]}`, x + cellW * 0.64, y + 34);
            });

            ctx.strokeStyle = `rgba(132,220,198,${0.18 + Math.sin(t * 2.4) * 0.08})`;
            ctx.lineWidth = 1.5;
            for (let i = 0; i < 4; i += 1) {
                ctx.beginPath();
                ctx.moveTo(-cellW * 2.8, -cellH * 1.5 + i * cellH);
                ctx.bezierCurveTo(-cellW, -cellH * 2 + i * 8, cellW, cellH * 2 - i * 8, cellW * 2.6, -cellH * 1.2 + i * cellH);
                ctx.stroke();
            }

            const scanY = -cellH * 2.1 + ((Math.sin(t * 1.2) + 1) / 2) * cellH * 4.4;
            const scanGradient = ctx.createLinearGradient(-cellW * 3, scanY, cellW * 3, scanY);
            scanGradient.addColorStop(0, 'rgba(132,220,198,0)');
            scanGradient.addColorStop(0.5, 'rgba(132,220,198,0.64)');
            scanGradient.addColorStop(1, 'rgba(132,220,198,0)');
            ctx.strokeStyle = scanGradient;
            ctx.lineWidth = 4;
            ctx.beginPath();
            ctx.moveTo(-cellW * 3.1, scanY);
            ctx.lineTo(cellW * 3.1, scanY);
            ctx.stroke();

            ctx.restore();
            ctx.restore();

            if (!prefersReducedMotion) {
                requestAnimationFrame(drawScene);
            }
        }

        window.addEventListener('resize', resizeFallback);
        window.addEventListener('scroll', updateFallbackScroll, { passive: true });
        window.addEventListener('mousemove', (event) => {
            pointerX = (event.clientX / window.innerWidth - 0.5) * 2;
            pointerY = (event.clientY / window.innerHeight - 0.5) * 2;
        }, { passive: true });

        resizeFallback();
        updateFallbackScroll();
        requestAnimationFrame(drawScene);
    }

    function initTimetableScene() {
        const canvas = document.getElementById('timetableScene');
        if (!canvas) {
            document.body.classList.add('no-webgl');
            return;
        }

        if (!window.THREE || !hasWebGL()) {
            document.body.classList.add('no-webgl');
            initCanvasFallback(canvas);
            return;
        }

        const THREE = window.THREE;
        const scene = new THREE.Scene();
        scene.fog = new THREE.FogExp2(0x071014, 0.035);

        const renderer = new THREE.WebGLRenderer({
            canvas,
            antialias: true,
            alpha: true,
            powerPreference: 'high-performance'
        });
        renderer.setClearColor(0x071014, 0);
        renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 1.8));
        renderer.setSize(window.innerWidth, window.innerHeight);

        const camera = new THREE.PerspectiveCamera(42, window.innerWidth / window.innerHeight, 0.1, 100);
        camera.position.set(0, 2.8, 10);

        const root = new THREE.Group();
        const schedule = new THREE.Group();
        const network = new THREE.Group();
        const conflicts = [];
        const blocks = [];
        const classCards = [];
        const connectionLines = [];
        const ambientNodes = [];
        const panels = [];
        let scanBeam = null;
        scene.add(root);
        root.add(schedule);
        root.add(network);

        const mouse = new THREE.Vector2(0, 0);
        const raycaster = new THREE.Raycaster();
        const pointerTarget = new THREE.Vector2(0, 0);
        let activeObject = null;
        let scrollProgress = 0;
        let activeStep = 0;
        let frame = 0;

        const colors = {
            mint: 0x84dcc6,
            teal: 0x2dd4bf,
            blue: 0x7db7ff,
            lavender: 0xb7a4ff,
            coral: 0xff9f8b,
            amber: 0xf8d36f,
            white: 0xf7fbfa
        };

        const compactSubjects = [
            'PHY', 'MATH', 'ENG', 'CS', 'CHEM', 'BIO',
            'AI', 'DSA', 'ECO', 'HIS', 'LAB', 'ELEC',
            'STAT', 'DBMS', 'OS', 'NET', 'ML', 'SE'
        ];

        scene.add(new THREE.AmbientLight(0xbfded8, 1.4));

        const keyLight = new THREE.DirectionalLight(0xffffff, 2.3);
        keyLight.position.set(4, 7, 6);
        scene.add(keyLight);

        const rimLight = new THREE.PointLight(colors.lavender, 3, 18);
        rimLight.position.set(-5, 1.5, 3);
        scene.add(rimLight);

        const fillLight = new THREE.PointLight(colors.teal, 2.4, 16);
        fillLight.position.set(4, -1, 5);
        scene.add(fillLight);

        createScheduleModel();
        createNetworkModel();
        createFieldLines();
        schedule.scale.set(1.16, 1.16, 1.16);

        window.addEventListener('resize', resize);
        window.addEventListener('scroll', updateScroll, { passive: true });
        window.addEventListener('mousemove', onPointerMove, { passive: true });
        window.addEventListener('click', onClick);

        updateScroll();
        resize();

        if (prefersReducedMotion) {
            renderStill();
            return;
        }

        animate();

        function createScheduleModel() {
            const glassMaterials = [
                makePanelMaterial(colors.mint, 0.15),
                makePanelMaterial(colors.blue, 0.12),
                makePanelMaterial(colors.lavender, 0.14)
            ];

            for (let i = 0; i < 3; i++) {
                const panel = new THREE.Mesh(
                    new THREE.BoxGeometry(6.8, 0.055, 3.9),
                    glassMaterials[i]
                );
                panel.position.y = i * 0.38;
                panel.rotation.x = -0.02;
                panel.userData = {
                    title: ['Room Grid Matrix', 'Course Sections', 'Faculty Assignment'][i],
                    details: ['Room capacity and availability are mapped first.', 'Subjects are placed against section demand.', 'Faculty workload is balanced across the week.'][i],
                    step: i + 1
                };
                schedule.add(panel);
                panels.push(panel);

                const edge = new THREE.LineSegments(
                    new THREE.EdgesGeometry(panel.geometry),
                    new THREE.LineBasicMaterial({ color: colors.white, transparent: true, opacity: 0.16 })
                );
                panel.add(edge);
            }

            const blockGeometry = new THREE.BoxGeometry(0.52, 0.18, 0.44);
            const palette = [colors.mint, colors.blue, colors.lavender, colors.teal, colors.amber];

            for (let layer = 0; layer < 3; layer++) {
                for (let row = 0; row < 4; row++) {
                    for (let col = 0; col < 6; col++) {
                        const slot = row * 6 + col;
                        if ((slot + layer) % 5 === 0) continue;

                        const material = new THREE.MeshPhysicalMaterial({
                            color: palette[(row + col + layer) % palette.length],
                            roughness: 0.36,
                            metalness: 0.08,
                            transmission: 0.18,
                            transparent: true,
                            opacity: 0.82,
                            emissive: palette[(row + col + layer) % palette.length],
                            emissiveIntensity: 0.07
                        });

                        const block = new THREE.Mesh(blockGeometry, material);
                        block.position.set(-2.7 + col * 1.08, layer * 0.38 + 0.14, -1.28 + row * 0.82);
                        block.rotation.y = (Math.random() - 0.5) * 0.12;
                        const subject = compactSubjects[(layer * 24 + slot) % compactSubjects.length];
                        const timeLabel = `${9 + (col % 6)}${col < 3 ? 'A' : 'P'}`;
                        block.userData = {
                            base: block.position.clone(),
                            layer,
                            row,
                            col,
                            subject,
                            timeLabel,
                            title: `${subject} - ${timeLabel} - Layer ${layer + 1}`,
                            details: 'Labeled timetable block checked against availability, capacity, and workload.',
                            step: 3 + layer
                        };
                        block.add(createCompactBlockLabel(subject, timeLabel, palette[(row + col + layer) % palette.length]));
                        schedule.add(block);
                        blocks.push(block);
                    }
                }
            }

            [
                ['Physics', 'Room 301', '9:00 AM', colors.mint, 0, 0],
                ['Mathematics', 'Room 201', '10:00 AM', colors.blue, 1, 0],
                ['English', 'Room 118', '11:00 AM', colors.coral, 2, 0],
                ['CS Lab', 'Lab 4', '1:00 PM', colors.lavender, 0, 1],
                ['Chemistry', 'Lab 2', '2:00 PM', colors.amber, 1, 1],
                ['Economics', 'Room 108', '3:00 PM', colors.teal, 2, 1]
            ].forEach((item, index) => {
                const card = createTimetableCard(item[0], item[1], item[2], item[3], index === 2);
                card.position.set(-2.2 + item[4] * 2.15, 1.04, -0.78 + item[5] * 1.08);
                card.userData = {
                    title: `${item[0]} - ${item[1]}`,
                    details: index === 2 ? 'This class is marked as a soft conflict before the optimizer resolves it.' : `${item[2]} placement is available for review.`,
                    step: index === 2 ? 2 : 5
                };
                schedule.add(card);
                classCards.push(card);
            });

            createScheduleConnections();
            createScanBeam();

            schedule.rotation.set(-0.62, 0, -0.52);
            schedule.position.set(1.7, 0.2, 0);
        }

        function createNetworkModel() {
            const nodeGeometry = new THREE.SphereGeometry(0.08, 18, 18);
            const nodeMaterial = new THREE.MeshPhysicalMaterial({
                color: colors.white,
                emissive: colors.teal,
                emissiveIntensity: 0.12,
                roughness: 0.25,
                metalness: 0.05,
                transparent: true,
                opacity: 0.22,
                depthWrite: false
            });
            const conflictMaterial = new THREE.MeshPhysicalMaterial({
                color: colors.coral,
                emissive: colors.coral,
                emissiveIntensity: 0.36,
                roughness: 0.24,
                transparent: true,
                opacity: 0.26,
                depthWrite: false
            });

            const nodes = [];
            for (let i = 0; i < 22; i++) {
                const radius = 2.2 + Math.random() * 2.4;
                const angle = (i / 22) * Math.PI * 2;
                const y = -1.6 + Math.random() * 3.6;
                const node = new THREE.Mesh(nodeGeometry, i % 7 === 0 ? conflictMaterial.clone() : nodeMaterial.clone());
                node.position.set(Math.cos(angle) * radius - 1.1, y, Math.sin(angle) * radius - 2.4);
                node.userData = {
                    title: i % 7 === 0 ? 'Conflict hotspot' : 'Constraint node',
                    details: i % 7 === 0 ? 'This overlap is being separated by the optimizer.' : 'A timetable rule connected to room, faculty, or subject data.',
                    step: i % 7 === 0 ? 2 : 1,
                    baseScale: 1,
                    base: node.position.clone(),
                    phase: i * 0.37
                };
                network.add(node);
                ambientNodes.push(node);
                nodes.push(node);
                if (i % 7 === 0) conflicts.push(node);
            }

            const lineMaterial = new THREE.LineBasicMaterial({
                color: colors.mint,
                transparent: true,
                opacity: 0.06,
                depthWrite: false
            });

            for (let i = 0; i < nodes.length - 1; i++) {
                if (i % 2 !== 0) continue;
                const points = [nodes[i].position, nodes[(i + 5) % nodes.length].position];
                const line = new THREE.Line(new THREE.BufferGeometry().setFromPoints(points), lineMaterial);
                network.add(line);
            }

            network.position.set(-2.6, 0.4, -1.7);
        }

        function createTimetableCard(subject, room, time, accent, isConflict) {
            const textureCanvas = document.createElement('canvas');
            textureCanvas.width = 512;
            textureCanvas.height = 256;
            const ctx = textureCanvas.getContext('2d');
            const accentHex = `#${accent.toString(16).padStart(6, '0')}`;

            ctx.clearRect(0, 0, textureCanvas.width, textureCanvas.height);
            ctx.fillStyle = isConflict ? 'rgba(72, 22, 22, 0.86)' : 'rgba(7, 17, 24, 0.88)';
            roundRect(ctx, 18, 18, 476, 220, 30);
            ctx.fill();
            ctx.strokeStyle = isConflict ? 'rgba(255, 159, 139, 0.80)' : accentHex;
            ctx.lineWidth = 4;
            ctx.stroke();

            ctx.fillStyle = '#f7fbfa';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.font = '800 42px Inter, Arial, sans-serif';
            ctx.fillText(subject, 256, 82);

            ctx.fillStyle = isConflict ? '#ffb5a5' : '#c8fff2';
            ctx.font = '700 28px Inter, Arial, sans-serif';
            ctx.fillText(room, 256, 136);

            ctx.fillStyle = 'rgba(247, 251, 250, 0.78)';
            ctx.font = '700 24px Inter, Arial, sans-serif';
            ctx.fillText(isConflict ? `${time}  CONFLICT` : time, 256, 184);

            const texture = new THREE.CanvasTexture(textureCanvas);
            if ('colorSpace' in texture && THREE.SRGBColorSpace) {
                texture.colorSpace = THREE.SRGBColorSpace;
            }
            texture.anisotropy = 4;

            const card = new THREE.Mesh(
                new THREE.PlaneGeometry(1.5, 0.72),
                new THREE.MeshBasicMaterial({
                    map: texture,
                    transparent: true,
                    depthWrite: false
                })
            );
            card.rotation.x = -Math.PI / 2;
            card.renderOrder = 12;
            return card;
        }

        function createCompactBlockLabel(subject, time, accent) {
            const labelCanvas = document.createElement('canvas');
            labelCanvas.width = 256;
            labelCanvas.height = 128;
            const ctx = labelCanvas.getContext('2d');
            const accentHex = `#${accent.toString(16).padStart(6, '0')}`;

            ctx.clearRect(0, 0, labelCanvas.width, labelCanvas.height);
            ctx.fillStyle = 'rgba(5, 12, 18, 0.70)';
            roundRect(ctx, 12, 12, 232, 104, 18);
            ctx.fill();
            ctx.strokeStyle = accentHex;
            ctx.lineWidth = 3;
            ctx.stroke();

            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillStyle = '#f7fbfa';
            ctx.font = '800 40px Inter, Arial, sans-serif';
            ctx.fillText(subject, 128, 52);
            ctx.fillStyle = 'rgba(247, 251, 250, 0.72)';
            ctx.font = '700 24px Inter, Arial, sans-serif';
            ctx.fillText(time, 128, 88);

            const texture = new THREE.CanvasTexture(labelCanvas);
            if ('colorSpace' in texture && THREE.SRGBColorSpace) {
                texture.colorSpace = THREE.SRGBColorSpace;
            }
            texture.anisotropy = 2;

            const label = new THREE.Mesh(
                new THREE.PlaneGeometry(0.56, 0.31),
                new THREE.MeshBasicMaterial({
                    map: texture,
                    transparent: true,
                    depthWrite: false
                })
            );
            label.rotation.x = -Math.PI / 2;
            label.position.y = 0.096;
            label.renderOrder = 14;
            return label;
        }

        function createScheduleConnections() {
            const lineMaterial = new THREE.LineBasicMaterial({
                color: colors.mint,
                transparent: true,
                opacity: 0.16,
                depthWrite: false
            });

            const routes = [
                [new THREE.Vector3(-2.2, 1.08, -0.78), new THREE.Vector3(0.0, 1.08, -0.78), new THREE.Vector3(2.1, 1.08, -0.78)],
                [new THREE.Vector3(-2.2, 1.08, 0.30), new THREE.Vector3(0.0, 1.08, 0.30), new THREE.Vector3(2.1, 1.08, 0.30)],
                [new THREE.Vector3(-2.72, 0.56, -1.28), new THREE.Vector3(-0.56, 0.88, -0.46), new THREE.Vector3(1.6, 1.04, 0.30)],
                [new THREE.Vector3(2.70, 0.18, 1.18), new THREE.Vector3(0.55, 0.70, 0.36), new THREE.Vector3(-1.6, 1.04, -0.78)]
            ];

            routes.forEach((points, index) => {
                const line = new THREE.Line(new THREE.BufferGeometry().setFromPoints(points), lineMaterial.clone());
                line.userData = {
                    phase: index * 0.7,
                    baseOpacity: index < 2 ? 0.18 : 0.12
                };
                line.renderOrder = 11;
                schedule.add(line);
                connectionLines.push(line);
            });
        }

        function createScanBeam() {
            scanBeam = new THREE.Mesh(
                new THREE.BoxGeometry(6.7, 0.018, 0.08),
                new THREE.MeshBasicMaterial({
                    color: colors.mint,
                    transparent: true,
                    opacity: 0.48,
                    depthWrite: false
                })
            );
            scanBeam.position.set(0, 1.15, -1.65);
            scanBeam.renderOrder = 13;
            schedule.add(scanBeam);
        }

        function roundRect(ctx, x, y, width, height, radius) {
            ctx.beginPath();
            ctx.moveTo(x + radius, y);
            ctx.lineTo(x + width - radius, y);
            ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
            ctx.lineTo(x + width, y + height - radius);
            ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
            ctx.lineTo(x + radius, y + height);
            ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
            ctx.lineTo(x, y + radius);
            ctx.quadraticCurveTo(x, y, x + radius, y);
            ctx.closePath();
        }

        function createFieldLines() {
            const lineMaterial = new THREE.LineBasicMaterial({
                color: colors.blue,
                transparent: true,
                opacity: 0.09
            });

            for (let i = 0; i < 11; i++) {
                const x = -5 + i;
                const vertical = new THREE.Line(
                    new THREE.BufferGeometry().setFromPoints([
                        new THREE.Vector3(x, -2.5, -4),
                        new THREE.Vector3(x, -2.5, 4)
                    ]),
                    lineMaterial
                );
                const horizontal = new THREE.Line(
                    new THREE.BufferGeometry().setFromPoints([
                        new THREE.Vector3(-5, -2.5, -4 + i * 0.8),
                        new THREE.Vector3(5, -2.5, -4 + i * 0.8)
                    ]),
                    lineMaterial
                );
                root.add(vertical, horizontal);
            }
        }

        function makePanelMaterial(color, opacity) {
            return new THREE.MeshPhysicalMaterial({
                color,
                transparent: true,
                opacity,
                roughness: 0.18,
                metalness: 0.05,
                transmission: 0.34,
                thickness: 0.6,
                side: THREE.DoubleSide
            });
        }

        function resize() {
            const width = window.innerWidth;
            const height = window.innerHeight;
            camera.aspect = width / height;
            camera.updateProjectionMatrix();
            renderer.setSize(width, height);
        }

        function updateScroll() {
            const max = document.documentElement.scrollHeight - window.innerHeight;
            scrollProgress = max > 0 ? window.scrollY / max : 0;
            const sections = document.querySelectorAll('[data-scene-step]');
            let closest = 0;
            let closestDistance = Infinity;
            sections.forEach((section) => {
                const rect = section.getBoundingClientRect();
                const distance = Math.abs(rect.top + rect.height * 0.35 - window.innerHeight * 0.45);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = Number(section.getAttribute('data-scene-step') || 0);
                }
            });
            activeStep = closest;
            if (window.updateSceneStatus) window.updateSceneStatus(activeStep);
        }

        function onPointerMove(event) {
            pointerTarget.x = (event.clientX / window.innerWidth) * 2 - 1;
            pointerTarget.y = -(event.clientY / window.innerHeight) * 2 + 1;
        }

        function onClick(event) {
            mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
            mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;
            raycaster.setFromCamera(mouse, camera);
            const hits = raycaster.intersectObjects([...classCards, ...blocks, ...panels, ...conflicts], false);
            if (!hits.length) return;

            activeObject = hits[0].object;
            if (window.updateSceneStatus) window.updateSceneStatus(activeObject.userData.step || activeStep);
            const sceneTitle = document.getElementById('sceneTitle');
            const sceneDetails = document.getElementById('sceneDetails');
            if (sceneTitle) sceneTitle.textContent = activeObject.userData.title;
            if (sceneDetails) sceneDetails.textContent = activeObject.userData.details;
        }

        function renderStill() {
            applySceneState(0.62);
            renderer.render(scene, camera);
        }

        function animate() {
            frame += 1;
            requestAnimationFrame(animate);
            mouse.x += (pointerTarget.x - mouse.x) * 0.055;
            mouse.y += (pointerTarget.y - mouse.y) * 0.055;

            applySceneState(scrollProgress);
            renderer.render(scene, camera);
        }

        function applySceneState(progress) {
            const time = frame * 0.012;
            const ambient = frame * 0.006;
            const stage = activeStep / 9;
            const conflictFade = Math.max(0, 1 - progress * 2.6);
            const resolved = Math.min(1, Math.max(0, progress * 2.2 - 0.65));
            const panelSpread = 1.15 - Math.min(1, progress * 1.65) * 0.95;

            root.rotation.y = -0.16 + Math.sin(ambient * 0.75) * 0.08 + mouse.x * 0.12 + progress * 0.35;
            root.rotation.x = -0.04 + Math.cos(ambient * 0.55) * 0.025 + mouse.y * 0.08;
            root.position.y = -0.2 + Math.sin(time * 0.7) * 0.05 + Math.sin(ambient) * 0.045 - progress * 0.18;

            schedule.position.x = 1.5 - progress * 2.4;
            schedule.position.y = 0.22 + Math.sin(time * 0.8) * 0.05 + Math.cos(ambient * 0.9) * 0.035;
            schedule.rotation.x = -0.62 + progress * 0.34;
            schedule.rotation.z = -0.52 + Math.sin(ambient * 0.48) * 0.035 + progress * 0.3;
            schedule.rotation.y = 0.16 + Math.cos(ambient * 0.62) * 0.055 + mouse.x * 0.12;

            network.position.x = -2.8 + progress * 1.1;
            network.rotation.y = time * 0.08 + Math.sin(ambient * 0.7) * 0.10 + mouse.x * 0.08;
            network.rotation.x = Math.cos(ambient * 0.8) * 0.04 + mouse.y * 0.05;

            panels.forEach((panel, index) => {
                panel.position.y = index * panelSpread;
                panel.material.opacity = 0.11 + index * 0.025 + progress * 0.02 + Math.sin(ambient * 1.2 + index) * 0.012;
            });

            blocks.forEach((block, index) => {
                const base = block.userData.base;
                const laneShift = Math.sin(index * 1.7) * (1 - resolved) * 0.42;
                const settleX = (block.userData.col - 2.5) * 0.03 * resolved;
                const settleZ = (block.userData.row - 1.5) * 0.03 * resolved;
                const ambientSlide = Math.sin(ambient * 1.2 + index * 0.37) * 0.035;
                block.position.x = base.x + laneShift + settleX + ambientSlide;
                block.position.y = base.y + Math.sin(time + index) * 0.018 * (1 - progress * 0.6) + Math.cos(ambient + index) * 0.012;
                block.position.z = base.z + Math.cos(time * 0.8 + index) * 0.025 + settleZ;
                block.rotation.y = Math.sin(time * 0.7 + index) * 0.04 * (1 - resolved) + Math.sin(ambient * 0.7 + index) * 0.015;

                const selected = activeObject === block ? 1 : 0;
                const scale = 1 + selected * 0.28 + Math.sin(time * 2 + index) * 0.015;
                block.scale.set(scale, scale, scale);
                block.material.emissiveIntensity = 0.08 + selected * 0.45 + stage * 0.02 + Math.max(0, Math.sin(ambient * 1.4 + index)) * 0.035;
            });

            classCards.forEach((card, index) => {
                const selected = activeObject === card ? 1 : 0;
                const hoverLift = Math.sin(time * 1.2 + index) * 0.018 + Math.cos(ambient * 1.1 + index) * 0.025;
                card.position.y = 1.04 + hoverLift + selected * 0.08;
                const scale = 1 + selected * 0.10 + Math.sin(time + index) * 0.01 + Math.max(0, Math.sin(ambient * 0.9 + index)) * 0.015;
                card.scale.set(scale, scale, scale);
            });

            connectionLines.forEach((line, index) => {
                const glow = Math.max(0, Math.sin(ambient * 2.2 + line.userData.phase));
                line.material.opacity = line.userData.baseOpacity + glow * 0.22 + stage * 0.03;
            });

            if (scanBeam) {
                const scan = (Math.sin(ambient * 1.35) + 1) / 2;
                scanBeam.position.z = -1.65 + scan * 3.30;
                scanBeam.material.opacity = Math.max(0.08, 0.22 + Math.sin(ambient * 1.35 + Math.PI / 2) * 0.26);
            }

            ambientNodes.forEach((node, index) => {
                const base = node.userData.base;
                node.position.x = base.x + Math.sin(ambient * 1.1 + node.userData.phase) * 0.09;
                node.position.y = base.y + Math.cos(ambient * 0.9 + node.userData.phase) * 0.07;
                node.position.z = base.z + Math.sin(ambient * 0.75 + index) * 0.08;
            });

            conflicts.forEach((node, index) => {
                const pulse = 1 + Math.sin(time * 3 + index) * 0.22;
                const scale = 1 + conflictFade * pulse + resolved * 0.25;
                node.scale.set(scale, scale, scale);
                node.material.color.setHex(resolved > 0.8 ? colors.mint : colors.coral);
                node.material.emissive.setHex(resolved > 0.8 ? colors.mint : colors.coral);
                node.material.emissiveIntensity = resolved > 0.8 ? 0.35 : 0.9 * conflictFade;
            });

            camera.position.x = mouse.x * 0.6 + Math.sin(ambient * 0.52) * 0.22;
            camera.position.y = 2.5 + mouse.y * 0.32 + Math.cos(ambient * 0.46) * 0.12 - progress * 0.25;
            camera.position.z = 10 + Math.sin(ambient * 0.38) * 0.20 - progress * 1.7;
            camera.lookAt(0, 0, 0);
        }
    }
})();
