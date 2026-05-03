(function () {
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (prefersReducedMotion) {
        document.body.classList.add('reduced-motion');
    }

    document.addEventListener('DOMContentLoaded', () => {
        initReveal();
        initHoverGlow();
        initSceneStatus();
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
            ['Live Optimization', 'Constraint field is forming', 'Rooms, faculty, and subjects are being mapped into a single scheduling space.', '12', '24', '68%'],
            ['Constraint Intake', 'Every rule becomes visible', 'Availability windows and room capacities separate into readable layers.', '9', '24', '71%'],
            ['Conflict Scan', 'Pressure points are highlighted', 'Overlaps glow before publication so planners can act early.', '5', '24', '76%'],
            ['Graph Coloring', 'The engine is resolving overlaps', 'Class blocks move into compatible time and room positions.', '1', '24', '80%'],
            ['Balanced Plan', 'The final grid is stabilizing', 'Faculty load, classroom use, and section coverage settle into a practical timetable.', '0', '24', '82%'],
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
        const viewport = canvas.parentElement || canvas;
        const scene = new THREE.Scene();
        scene.fog = new THREE.FogExp2(0x0a0e1a, 0.035);

        const renderer = new THREE.WebGLRenderer({
            canvas,
            antialias: true,
            alpha: true,
            powerPreference: 'high-performance'
        });
        renderer.setClearColor(0x0a0e1a, 0);
        renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 1.8));

        const initialSize = getViewportSize();
        const camera = new THREE.PerspectiveCamera(42, initialSize.width / initialSize.height, 0.1, 100);
        camera.position.set(0, 2.8, 10);

        const root = new THREE.Group();
        const schedule = new THREE.Group();
        const network = new THREE.Group();
        const conflicts = [];
        const blocks = [];
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

        const timetableLabels = [
            'Physics · Room 301 · 9AM',
            'Math · Room 201 · 10AM',
            'English · 11AM ⚡ Conflict',
            'Chemistry · Lab 2 · 12PM',
            'CS · Room 402 · 2PM',
            'Biology · Lab 1 · 1PM',
            'Economics · Room 108 · 3PM',
            'History · Room 204 · 4PM',
            'AI Lab · Lab 4 · 2PM',
            'Design · Studio 1 · 1PM',
            'Data Structures · Room 305 · 4PM',
            'Electronics · Lab 3 · 10AM'
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
                    new THREE.BoxGeometry(7.2, 0.055, 4.15),
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

            const blockGeometry = new THREE.BoxGeometry(0.82, 0.2, 0.56);
            const palette = [colors.mint, colors.blue, colors.lavender, colors.teal, colors.amber];

            for (let layer = 0; layer < 3; layer++) {
                for (let row = 0; row < 4; row++) {
                    for (let col = 0; col < 6; col++) {
                        const slot = row * 6 + col;
                        if ((slot + layer) % 5 === 0) continue;

                        const color = palette[(row + col + layer) % palette.length];
                        const labelText = timetableLabels[(layer * 24 + slot) % timetableLabels.length];
                        const hasConflict = labelText.includes('Conflict');

                        const material = new THREE.MeshPhysicalMaterial({
                            color,
                            roughness: 0.36,
                            metalness: 0.08,
                            transmission: 0.18,
                            transparent: true,
                            opacity: 0.88,
                            emissive: hasConflict ? colors.coral : color,
                            emissiveIntensity: hasConflict ? 0.18 : 0.08
                        });

                        const block = new THREE.Mesh(blockGeometry, material);
                        block.position.set(-2.85 + col * 1.14, layer * 0.38 + 0.14, -1.32 + row * 0.86);
                        block.rotation.y = (Math.random() - 0.5) * 0.12;
                        block.userData = {
                            base: block.position.clone(),
                            layer,
                            row,
                            col,
                            labelText,
                            title: labelText,
                            details: 'Selected timetable slot is checked against availability, capacity, and workload.',
                            step: 3 + layer
                        };
                        block.add(createBlockLabel(labelText, hasConflict));
                        schedule.add(block);
                        blocks.push(block);
                    }
                }
            }

            schedule.rotation.set(-0.62, 0, -0.52);
            schedule.position.set(0.45, 0.2, 0);
        }

        function createNetworkModel() {
            const nodeGeometry = new THREE.SphereGeometry(0.066, 16, 16);
            const nodeMaterial = new THREE.MeshPhysicalMaterial({
                color: colors.white,
                emissive: colors.teal,
                emissiveIntensity: 0.08,
                roughness: 0.25,
                metalness: 0.05,
                transparent: true,
                opacity: 0.15,
                depthWrite: false
            });
            const conflictMaterial = new THREE.MeshPhysicalMaterial({
                color: colors.coral,
                emissive: colors.coral,
                emissiveIntensity: 0.18,
                roughness: 0.24,
                transparent: true,
                opacity: 0.15,
                depthWrite: false
            });

            const nodes = [];
            for (let i = 0; i < 30; i++) {
                const radius = 2.2 + Math.random() * 2.4;
                const angle = (i / 30) * Math.PI * 2;
                const y = -1.6 + Math.random() * 3.6;
                const node = new THREE.Mesh(nodeGeometry, i % 7 === 0 ? conflictMaterial.clone() : nodeMaterial.clone());
                node.position.set(Math.cos(angle) * radius - 1.1, y, Math.sin(angle) * radius - 0.7);
                node.renderOrder = -10;
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
                opacity: 0.045,
                depthWrite: false
            });

            for (let i = 0; i < nodes.length - 1; i++) {
                if (i % 2 !== 0) continue;
                const points = [nodes[i].position, nodes[(i + 5) % nodes.length].position];
                const line = new THREE.Line(new THREE.BufferGeometry().setFromPoints(points), lineMaterial);
                network.add(line);
            }

            network.position.set(-1.55, 0.15, -4.4);
            network.scale.setScalar(0.62);
        }

        function createBlockLabel(text, hasConflict) {
            const labelCanvas = document.createElement('canvas');
            labelCanvas.width = 512;
            labelCanvas.height = 256;
            const ctx = labelCanvas.getContext('2d');
            const fill = hasConflict ? '#ffdfd5' : '#ffffff';
            const stroke = hasConflict ? 'rgba(255, 159, 139, 0.86)' : 'rgba(255, 255, 255, 0.32)';
            const bg = hasConflict ? 'rgba(72, 17, 18, 0.62)' : 'rgba(6, 12, 20, 0.58)';

            ctx.clearRect(0, 0, labelCanvas.width, labelCanvas.height);
            drawRoundRect(ctx, 20, 26, 472, 204, 28, bg, stroke);

            const parts = text.split(' · ');
            const title = parts[0] || text;
            const detail = parts.slice(1).join(' · ');

            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillStyle = fill;
            setFittedFont(ctx, title, 396, 38, 25);
            ctx.fillText(title, 256, detail ? 96 : 128);

            if (detail) {
                ctx.fillStyle = hasConflict ? '#ffb29f' : '#dffcf4';
                setFittedFont(ctx, detail, 420, 31, 21);
                ctx.fillText(detail, 256, 152);
            }

            const texture = new THREE.CanvasTexture(labelCanvas);
            if ('colorSpace' in texture && THREE.SRGBColorSpace) {
                texture.colorSpace = THREE.SRGBColorSpace;
            }
            texture.anisotropy = 4;

            const material = new THREE.MeshBasicMaterial({
                map: texture,
                transparent: true,
                depthWrite: false
            });
            const label = new THREE.Mesh(new THREE.PlaneGeometry(0.78, 0.39), material);
            label.rotation.x = -Math.PI / 2;
            label.position.y = 0.106;
            label.renderOrder = 20;
            return label;
        }

        function drawRoundRect(ctx, x, y, width, height, radius, fill, stroke) {
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
            ctx.fillStyle = fill;
            ctx.fill();
            ctx.strokeStyle = stroke;
            ctx.lineWidth = 3;
            ctx.stroke();
        }

        function setFittedFont(ctx, text, maxWidth, startSize, minSize) {
            let size = startSize;
            do {
                ctx.font = `800 ${size}px Inter, Arial, sans-serif`;
                size -= 1;
            } while (ctx.measureText(text).width > maxWidth && size >= minSize);
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
            const { width, height } = getViewportSize();
            camera.aspect = width / height;
            camera.updateProjectionMatrix();
            renderer.setSize(width, height, false);
        }

        function getViewportSize() {
            const rect = viewport.getBoundingClientRect();
            return {
                width: Math.max(1, Math.floor(rect.width || window.innerWidth * 0.5)),
                height: Math.max(1, Math.floor(rect.height || window.innerHeight))
            };
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
            const pointer = getCanvasPointer(event, true);
            pointerTarget.x = pointer.x;
            pointerTarget.y = pointer.y;
        }

        function onClick(event) {
            const pointer = getCanvasPointer(event, false);
            if (!pointer) return;
            raycaster.setFromCamera(pointer, camera);
            const hits = raycaster.intersectObjects([...blocks, ...panels, ...conflicts], false);
            if (!hits.length) return;

            activeObject = hits[0].object;
            if (window.updateSceneStatus) window.updateSceneStatus(activeObject.userData.step || activeStep);
            const sceneTitle = document.getElementById('sceneTitle');
            const sceneDetails = document.getElementById('sceneDetails');
            if (sceneTitle) sceneTitle.textContent = activeObject.userData.title;
            if (sceneDetails) sceneDetails.textContent = activeObject.userData.details;
        }

        function getCanvasPointer(event, clampToBounds) {
            const rect = canvas.getBoundingClientRect();
            const rawX = (event.clientX - rect.left) / rect.width;
            const rawY = (event.clientY - rect.top) / rect.height;
            if (!clampToBounds && (rawX < 0 || rawX > 1 || rawY < 0 || rawY > 1)) {
                return null;
            }
            const normalizedX = clampToBounds ? Math.min(1, Math.max(0, rawX)) : rawX;
            const normalizedY = clampToBounds ? Math.min(1, Math.max(0, rawY)) : rawY;
            return new THREE.Vector2(normalizedX * 2 - 1, -(normalizedY * 2 - 1));
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
            const stage = activeStep / 6;
            const conflictFade = Math.max(0, 1 - progress * 2.6);
            const resolved = Math.min(1, Math.max(0, progress * 2.2 - 0.65));
            const panelSpread = 1.15 - Math.min(1, progress * 1.65) * 0.95;

            root.rotation.y = -0.16 + mouse.x * 0.12 + progress * 0.35;
            root.rotation.x = -0.04 + mouse.y * 0.08;
            root.position.y = -0.2 + Math.sin(time * 0.7) * 0.05 - progress * 0.18;

            schedule.position.x = 0.45 - progress * 0.8;
            schedule.position.y = 0.22 + Math.sin(time * 0.8) * 0.05;
            schedule.rotation.x = -0.62 + progress * 0.34;
            schedule.rotation.z = -0.52 + progress * 0.3;
            schedule.rotation.y = 0.16 + mouse.x * 0.12;

            network.position.x = -1.55 + progress * 0.55;
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

            conflicts.forEach((node, index) => {
                const pulse = 1 + Math.sin(time * 3 + index) * 0.22;
                const scale = 1 + conflictFade * pulse + resolved * 0.25;
                node.scale.set(scale, scale, scale);
                node.material.color.setHex(resolved > 0.8 ? colors.mint : colors.coral);
                node.material.emissive.setHex(resolved > 0.8 ? colors.mint : colors.coral);
                node.material.opacity = 0.15;
                node.material.emissiveIntensity = resolved > 0.8 ? 0.12 : 0.18 * conflictFade;
            });

            camera.position.x = mouse.x * 0.32;
            camera.position.y = 2.5 + mouse.y * 0.22 - progress * 0.25;
            camera.position.z = 10 - progress * 1.7;
            camera.lookAt(0, 0, 0);
        }
    }
})();
