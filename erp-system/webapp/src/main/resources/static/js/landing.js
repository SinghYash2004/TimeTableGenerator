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
        let sweepTimer;

        function setActive(nextIndex) {
            if (nextIndex === activeIndex) return;
            activeIndex = nextIndex;
            const progress = phases.length === 1 ? 0 : (activeIndex / (phases.length - 1)) * 100;
            story.style.setProperty('--timeline-progress', `${progress}%`);
            story.style.setProperty('--timeline-sweep', `${Math.max(0, Math.min(100, progress))}%`);
            story.classList.remove('is-traveling');
            void story.offsetWidth;
            story.classList.add('is-traveling');
            window.clearTimeout(sweepTimer);
            sweepTimer = window.setTimeout(() => story.classList.remove('is-traveling'), 780);

            phases.forEach((phase, index) => {
                phase.classList.toggle('is-active', index === activeIndex);
                phase.classList.toggle('is-past', index < activeIndex);
            });
        }

        const observer = new IntersectionObserver((entries) => {
            let bestEntry = null;
            entries.forEach((entry) => {
                if (!entry.isIntersecting) return;
                if (!bestEntry || entry.intersectionRatio > bestEntry.intersectionRatio) {
                    bestEntry = entry;
                }
            });
            if (!bestEntry) return;
            setActive(Number(bestEntry.target.getAttribute('data-phase-index') || 0));
        }, { threshold: [0.32, 0.48, 0.64], rootMargin: '-22% 0px -28% 0px' });

        phases.forEach((phase, index) => {
            phase.classList.toggle('is-active', index === 0);
            observer.observe(phase);
        });
        story.style.setProperty('--timeline-progress', '0%');
        story.style.setProperty('--timeline-sweep', '0%');
    }

    function hasWebGL() {
        try {
            const canvas = document.createElement('canvas');
            return Boolean(window.WebGLRenderingContext && (canvas.getContext('webgl') || canvas.getContext('experimental-webgl')));
        } catch (error) {
            return false;
        }
    }

    function initTimetableScene() {
        const canvas = document.getElementById('timetableScene');
        if (!canvas || !window.THREE || !hasWebGL()) {
            document.body.classList.add('no-webgl');
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
        const panels = [];
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
                        block.userData = {
                            base: block.position.clone(),
                            layer,
                            row,
                            col,
                            title: `${['Room', 'Subject', 'Faculty'][layer]} block ${row + 1}-${col + 1}`,
                            details: 'Selected timetable slot is checked against availability, capacity, and workload.',
                            step: 3 + layer
                        };
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
                    baseScale: 1
                };
                network.add(node);
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
            const stage = activeStep / 9;
            const conflictFade = Math.max(0, 1 - progress * 2.6);
            const resolved = Math.min(1, Math.max(0, progress * 2.2 - 0.65));
            const panelSpread = 1.15 - Math.min(1, progress * 1.65) * 0.95;

            root.rotation.y = -0.16 + mouse.x * 0.12 + progress * 0.35;
            root.rotation.x = -0.04 + mouse.y * 0.08;
            root.position.y = -0.2 + Math.sin(time * 0.7) * 0.05 - progress * 0.18;

            schedule.position.x = 1.5 - progress * 2.4;
            schedule.position.y = 0.22 + Math.sin(time * 0.8) * 0.05;
            schedule.rotation.x = -0.62 + progress * 0.34;
            schedule.rotation.z = -0.52 + progress * 0.3;
            schedule.rotation.y = 0.16 + mouse.x * 0.12;

            network.position.x = -2.8 + progress * 1.1;
            network.rotation.y = time * 0.08 + mouse.x * 0.08;
            network.rotation.x = mouse.y * 0.05;

            panels.forEach((panel, index) => {
                panel.position.y = index * panelSpread;
                panel.material.opacity = 0.11 + index * 0.025 + progress * 0.02;
            });

            blocks.forEach((block, index) => {
                const base = block.userData.base;
                const laneShift = Math.sin(index * 1.7) * (1 - resolved) * 0.42;
                const settleX = (block.userData.col - 2.5) * 0.03 * resolved;
                const settleZ = (block.userData.row - 1.5) * 0.03 * resolved;
                block.position.x = base.x + laneShift + settleX;
                block.position.y = base.y + Math.sin(time + index) * 0.018 * (1 - progress * 0.6);
                block.position.z = base.z + Math.cos(time * 0.8 + index) * 0.025 + settleZ;
                block.rotation.y = Math.sin(time * 0.7 + index) * 0.04 * (1 - resolved);

                const selected = activeObject === block ? 1 : 0;
                const scale = 1 + selected * 0.28 + Math.sin(time * 2 + index) * 0.015;
                block.scale.set(scale, scale, scale);
                block.material.emissiveIntensity = 0.08 + selected * 0.45 + stage * 0.02;
            });

            classCards.forEach((card, index) => {
                const selected = activeObject === card ? 1 : 0;
                const hoverLift = Math.sin(time * 1.2 + index) * 0.018;
                card.position.y = 1.04 + hoverLift + selected * 0.08;
                const scale = 1 + selected * 0.10 + Math.sin(time + index) * 0.01;
                card.scale.set(scale, scale, scale);
            });

            conflicts.forEach((node, index) => {
                const pulse = 1 + Math.sin(time * 3 + index) * 0.22;
                const scale = 1 + conflictFade * pulse + resolved * 0.25;
                node.scale.set(scale, scale, scale);
                node.material.color.setHex(resolved > 0.8 ? colors.mint : colors.coral);
                node.material.emissive.setHex(resolved > 0.8 ? colors.mint : colors.coral);
                node.material.emissiveIntensity = resolved > 0.8 ? 0.35 : 0.9 * conflictFade;
            });

            camera.position.x = mouse.x * 0.6;
            camera.position.y = 2.5 + mouse.y * 0.32 - progress * 0.25;
            camera.position.z = 10 - progress * 1.7;
            camera.lookAt(0, 0, 0);
        }
    }
})();
