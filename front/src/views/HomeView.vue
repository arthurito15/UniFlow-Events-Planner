<template>
  <div class="home-page">
    <header class="landing-header">
      <img
          src="/src/assets/logo-app.png"
          alt="Logo"
          class="logo-img"
          @click="goHome"
      />

      <nav class="nav-links">
        <a href="#home">Home</a>
        <a href="#about">About us</a>
        <a href="#contact">Contact us</a>
      </nav>

      <div class="auth-actions">
        <button class="ghost-btn" @click="goToLogin">Connexion</button>
        <button class="primary-btn" @click="goToRegister">Inscription</button>
      </div>
    </header>

    <section id="home" class="hero-section">
      <div class="hero-content">
        <p class="eyebrow">Plateforme événementielle universitaire</p>
        <h1>
          Découvrez, organisez et vivez
          <span>les événements de votre campus</span>
        </h1>
        <p class="hero-text">
          UniFlow vous permet de retrouver facilement les événements étudiants,
          culturels et universitaires, tout en offrant une gestion simple pour
          les organisateurs.
        </p>

        <div class="hero-actions">
          <button class="primary-btn" @click="goToRegister">
            Commencer maintenant
          </button>
          <button class="secondary-btn" @click="goToLogin">
            Se connecter
          </button>
        </div>
      </div>

      <div class="hero-image-wrapper">
        <img
            class="hero-image"
            src="https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=1200&q=80"
            alt="Étudiants sur un campus"
        />
      </div>
    </section>

    <section id="about" class="info-section">
      <div class="section-title">
        <p class="eyebrow">About us</p>
        <h2>Une plateforme pensée pour la vie universitaire</h2>
      </div>

      <div class="cards-grid">
        <article class="info-card">
          <div class="icon">🎓</div>
          <h3>Pour les étudiants</h3>
          <p>
            Retrouvez rapidement les événements qui vous intéressent selon vos
            pôles, vos envies et votre campus.
          </p>
        </article>

        <article class="info-card">
          <div class="icon">📅</div>
          <h3>Pour les organisateurs</h3>
          <p>
            Créez, gérez et suivez vos événements dans une interface moderne et
            simple à utiliser.
          </p>
        </article>

        <article class="info-card">
          <div class="icon">🚀</div>
          <h3>Pour l’université</h3>
          <p>
            Centralisez les activités étudiantes, associatives et académiques
            dans une seule application.
          </p>
        </article>
      </div>
    </section>

    <section id="contact" class="contact-section">
      <div class="contact-card">
        <div>
          <p class="eyebrow">Contact us</p>
          <h2>Vous avez une question ?</h2>
          <p class="contact-text">
            Contactez l’équipe UniFlow pour toute demande d’information,
            d’assistance ou de partenariat.
          </p>
        </div>

        <div class="contact-info">
          <p><strong>Email :</strong> contact@uniflow.fr</p>
          <p><strong>Téléphone :</strong> +33 6 00 00 00 00</p>
          <p><strong>Adresse :</strong> Campus Lyon 1</p>
        </div>
      </div>
    </section>
    <AuthFooter />
  </div>

</template>

<script setup>
import { useRouter } from 'vue-router'
import AuthFooter from "@/components/AuthFooter.vue";

const router = useRouter()

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background:
      radial-gradient(circle at top left, rgba(124, 58, 237, 0.12), transparent 30%),
      linear-gradient(180deg, #f8f6fc 0%, #ffffff 100%);
  color: #24192f;
}

.landing-header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #eee8f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 40px;
  flex-wrap: wrap;
  gap: 16px;
}

.logo {
  font-size: 24px;
  font-weight: 800;
  color: #6d28d9;
}

.nav-links {
  display: flex;
  gap: 22px;
  flex-wrap: wrap;
}

.nav-links a {
  text-decoration: none;
  color: #5c4b74;
  font-weight: 600;
}

.auth-actions {
  display: flex;
  gap: 10px;
}

.hero-section {
  max-width: 1280px;
  margin: 0 auto;
  padding: 56px 40px;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 32px;
  align-items: center;
}

.hero-content h1 {
  margin: 0;
  font-size: 52px;
  line-height: 1.08;
  color: #20152c;
}

.hero-content h1 span {
  color: #7c3aed;
}

.eyebrow {
  margin: 0 0 10px;
  color: #8b7aa3;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.hero-text {
  margin: 22px 0 0;
  font-size: 18px;
  line-height: 1.7;
  color: #6f657b;
  max-width: 620px;
}

.hero-actions {
  display: flex;
  gap: 14px;
  margin-top: 28px;
  flex-wrap: wrap;
}

.primary-btn {
  border: none;
  background: #7c3aed;
  color: white;
  border-radius: 14px;
  padding: 13px 20px;
  font-weight: 700;
  box-shadow: 0 12px 24px rgba(124, 58, 237, 0.18);
}

.secondary-btn,
.ghost-btn {
  border: 1px solid #ddd6e6;
  background: white;
  color: #5c4b74;
  border-radius: 14px;
  padding: 13px 20px;
  font-weight: 700;
}

.hero-image-wrapper {
  display: flex;
  justify-content: center;
}

.hero-image {
  width: 100%;
  max-width: 560px;
  height: 620px;
  object-fit: cover;
  border-radius: 28px;
  box-shadow: 0 28px 60px rgba(31, 16, 41, 0.14);
}

.info-section,
.contact-section {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 40px 64px;
}

.section-title h2,
.contact-card h2 {
  margin: 0;
  font-size: 34px;
  color: #24192f;
}

.cards-grid {
  margin-top: 26px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.info-card {
  background: white;
  border: 1px solid #eee8f5;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 14px 34px rgba(25, 15, 35, 0.04);
}

.icon {
  font-size: 30px;
  margin-bottom: 14px;
}

.info-card h3 {
  margin: 0 0 10px;
}

.info-card p,
.contact-text,
.contact-info p {
  color: #6f657b;
  line-height: 1.6;
}

.contact-card {
  background: linear-gradient(135deg, #ffffff 0%, #faf7ff 100%);
  border: 1px solid #eee8f5;
  border-radius: 28px;
  padding: 28px;
  box-shadow: 0 14px 34px rgba(25, 15, 35, 0.04);
  display: flex;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

@media (max-width: 1024px) {
  .hero-section {
    grid-template-columns: 1fr;
  }

  .hero-content h1 {
    font-size: 42px;
  }

  .cards-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .landing-header,
  .hero-section,
  .info-section,
  .contact-section {
    padding-left: 18px;
    padding-right: 18px;
  }

  .landing-header {
    justify-content: center;
  }

  .hero-content h1 {
    font-size: 34px;
  }

  .hero-image {
    height: 420px;
  }
}
</style>