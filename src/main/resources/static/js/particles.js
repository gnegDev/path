/**
 * VeraAI — Particle + DNA Helix Background Animation
 * Ported from ParticleBackground.tsx (React/Canvas → Vanilla JS)
 */

class ParticleBackground {
  constructor(canvasId) {
    this.canvas = document.getElementById(canvasId);
    if (!this.canvas) return;
    this.ctx = this.canvas.getContext('2d');
    this.particles = [];
    this.mouse = { x: null, y: null };
    this.animId = null;
    this.MAX_PARTICLES = 120;
    this.CONNECTION_DIST = 150;
    this.MOUSE_DIST = 200;
    this.DNA_STEPS = 40;
    this.time = 0;

    this.resize = this.resize.bind(this);
    this.animate = this.animate.bind(this);
    this.onMouseMove = this.onMouseMove.bind(this);
    this.onMouseLeave = this.onMouseLeave.bind(this);

    this.init();
  }

  init() {
    this.resize();
    window.addEventListener('resize', this.resize);
    this.canvas.addEventListener('mousemove', this.onMouseMove);
    this.canvas.addEventListener('mouseleave', this.onMouseLeave);
    this.spawnParticles();
    this.animate();
  }

  resize() {
    this.canvas.width = this.canvas.offsetWidth;
    this.canvas.height = this.canvas.offsetHeight;
  }

  onMouseMove(e) {
    const rect = this.canvas.getBoundingClientRect();
    this.mouse.x = e.clientX - rect.left;
    this.mouse.y = e.clientY - rect.top;
  }

  onMouseLeave() {
    this.mouse.x = null;
    this.mouse.y = null;
  }

  spawnParticles() {
    for (let i = 0; i < this.MAX_PARTICLES; i++) {
      this.particles.push(this.createParticle());
    }
  }

  createParticle() {
    return {
      x: Math.random() * this.canvas.width,
      y: Math.random() * this.canvas.height,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      radius: Math.random() * 2 + 1,
      opacity: Math.random() * 0.5 + 0.2,
    };
  }

  updateParticle(p) {
    p.x += p.vx;
    p.y += p.vy;

    if (p.x < 0) p.x = this.canvas.width;
    if (p.x > this.canvas.width) p.x = 0;
    if (p.y < 0) p.y = this.canvas.height;
    if (p.y > this.canvas.height) p.y = 0;

    // Mouse repulsion
    if (this.mouse.x !== null) {
      const dx = p.x - this.mouse.x;
      const dy = p.y - this.mouse.y;
      const dist = Math.sqrt(dx * dx + dy * dy);
      if (dist < this.MOUSE_DIST) {
        const force = (this.MOUSE_DIST - dist) / this.MOUSE_DIST;
        p.x += (dx / dist) * force * 2;
        p.y += (dy / dist) * force * 2;
      }
    }
  }

  drawParticle(p) {
    this.ctx.beginPath();
    this.ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
    this.ctx.fillStyle = `rgba(0, 201, 167, ${p.opacity})`;
    this.ctx.fill();
  }

  drawConnections() {
    for (let i = 0; i < this.particles.length; i++) {
      for (let j = i + 1; j < this.particles.length; j++) {
        const p1 = this.particles[i];
        const p2 = this.particles[j];
        const dx = p1.x - p2.x;
        const dy = p1.y - p2.y;
        const dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < this.CONNECTION_DIST) {
          const opacity = (1 - dist / this.CONNECTION_DIST) * 0.2;
          this.ctx.beginPath();
          this.ctx.moveTo(p1.x, p1.y);
          this.ctx.lineTo(p2.x, p2.y);
          this.ctx.strokeStyle = `rgba(0, 201, 167, ${opacity})`;
          this.ctx.lineWidth = 0.8;
          this.ctx.stroke();
        }
      }
    }
  }

  drawDNAHelix() {
    const cx = this.canvas.width / 2;
    const cy = this.canvas.height / 2;
    const helixHeight = 280;
    const helixWidth = 50;
    const stepHeight = helixHeight / this.DNA_STEPS;

    this.ctx.save();

    for (let i = 0; i < this.DNA_STEPS; i++) {
      const progress = i / this.DNA_STEPS;
      const y = cy - helixHeight / 2 + progress * helixHeight;
      const phase = progress * Math.PI * 4 + this.time * 0.4;

      // Strand 1
      const x1 = cx + Math.cos(phase) * helixWidth;
      // Strand 2
      const x2 = cx + Math.cos(phase + Math.PI) * helixWidth;

      const depthOpacity1 = (Math.cos(phase) + 1) / 2;
      const depthOpacity2 = (Math.cos(phase + Math.PI) + 1) / 2;

      // Dots on strands
      this.ctx.beginPath();
      this.ctx.arc(x1, y, 3.5, 0, Math.PI * 2);
      this.ctx.fillStyle = `rgba(0, 201, 167, ${0.3 + depthOpacity1 * 0.7})`;
      this.ctx.fill();

      this.ctx.beginPath();
      this.ctx.arc(x2, y, 3.5, 0, Math.PI * 2);
      this.ctx.fillStyle = `rgba(96, 165, 250, ${0.3 + depthOpacity2 * 0.7})`;
      this.ctx.fill();

      // Connecting rungs (every 4 steps)
      if (i % 4 === 0) {
        const rungOpacity = Math.min(depthOpacity1, depthOpacity2) * 0.35;
        this.ctx.beginPath();
        this.ctx.moveTo(x1, y);
        this.ctx.lineTo(x2, y);
        this.ctx.strokeStyle = `rgba(0, 201, 167, ${rungOpacity})`;
        this.ctx.lineWidth = 1;
        this.ctx.stroke();
      }

      // Strand line segments
      if (i > 0) {
        const prevProgress = (i - 1) / this.DNA_STEPS;
        const prevY = cy - helixHeight / 2 + prevProgress * helixHeight;
        const prevPhase = prevProgress * Math.PI * 4 + this.time * 0.4;
        const prevX1 = cx + Math.cos(prevPhase) * helixWidth;
        const prevX2 = cx + Math.cos(prevPhase + Math.PI) * helixWidth;

        this.ctx.beginPath();
        this.ctx.moveTo(prevX1, prevY);
        this.ctx.lineTo(x1, y);
        this.ctx.strokeStyle = `rgba(0, 201, 167, ${0.15 + depthOpacity1 * 0.2})`;
        this.ctx.lineWidth = 1.5;
        this.ctx.stroke();

        this.ctx.beginPath();
        this.ctx.moveTo(prevX2, prevY);
        this.ctx.lineTo(x2, y);
        this.ctx.strokeStyle = `rgba(96, 165, 250, ${0.15 + depthOpacity2 * 0.2})`;
        this.ctx.lineWidth = 1.5;
        this.ctx.stroke();
      }
    }

    this.ctx.restore();
  }

  animate() {
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

    this.particles.forEach(p => {
      this.updateParticle(p);
      this.drawParticle(p);
    });

    this.drawConnections();
    this.drawDNAHelix();

    this.time += 0.016;
    this.animId = requestAnimationFrame(this.animate);
  }

  destroy() {
    if (this.animId) cancelAnimationFrame(this.animId);
    window.removeEventListener('resize', this.resize);
  }
}

// Auto-init on DOMContentLoaded
document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById('particle-canvas')) {
    new ParticleBackground('particle-canvas');
  }
});
