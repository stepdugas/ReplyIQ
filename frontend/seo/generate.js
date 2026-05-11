import fs from 'fs'
import path from 'path'
import { categories, cities, competitors, howtos } from './data.js'

const SITE_URL = 'https://www.replyiqapp.com'
const outputDir = path.resolve('public')

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true })
}

// Shared HTML layout
function layout({ title, metaDescription, canonicalPath, body }) {
  return `<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${title}</title>
<meta name="description" content="${metaDescription}">
<meta property="og:title" content="${title}">
<meta property="og:description" content="${metaDescription}">
<meta property="og:type" content="article">
<link rel="canonical" href="${SITE_URL}${canonicalPath}">
<link rel="icon" type="image/svg+xml" href="/favicon.svg">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:'Inter',system-ui,sans-serif;background:#0f1320;color:#d1d5db;-webkit-font-smoothing:antialiased;line-height:1.6}
a{color:#34d399;text-decoration:none}
a:hover{color:#6ee7b7}
.nav{position:sticky;top:0;z-index:50;background:rgba(15,19,32,.8);backdrop-filter:blur(20px);border-bottom:1px solid #2a3040;padding:16px 24px}
.nav-inner{max-width:1100px;margin:0 auto;display:flex;align-items:center;justify-content:space-between}
.logo{display:flex;align-items:center;gap:10px;color:#fff;font-weight:700;font-size:18px;text-decoration:none}
.logo-icon{width:32px;height:32px;background:linear-gradient(135deg,#10b981,#14b8a6);border-radius:8px;display:flex;align-items:center;justify-content:center;color:#fff;font-weight:700;font-size:14px}
.nav-links{display:flex;align-items:center;gap:12px}
.nav-links a{font-size:14px;color:#9ca3af}
.btn{display:inline-block;padding:10px 24px;background:#10b981;color:#fff;font-weight:600;font-size:14px;border-radius:10px;transition:background .2s}
.btn:hover{background:#059669;color:#fff}
.btn-lg{padding:14px 32px;font-size:16px;border-radius:12px}
.container{max-width:800px;margin:0 auto;padding:0 24px}
.hero{text-align:center;padding:80px 24px 60px}
.hero h1{font-size:clamp(28px,5vw,48px);font-weight:700;color:#fff;line-height:1.15;margin-bottom:16px}
.hero p{font-size:18px;color:#9ca3af;max-width:600px;margin:0 auto 32px;line-height:1.6}
.section{padding:60px 24px;border-top:1px solid #1a1f2e}
.section-title{font-size:28px;font-weight:700;color:#fff;text-align:center;margin-bottom:40px}
.steps{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:24px;max-width:900px;margin:0 auto}
.step{text-align:center}
.step-num{width:48px;height:48px;margin:0 auto 16px;background:rgba(16,185,129,.1);border:1px solid rgba(16,185,129,.2);border-radius:14px;display:flex;align-items:center;justify-content:center;color:#34d399;font-weight:700;font-size:18px}
.step h3{color:#fff;font-size:15px;font-weight:600;margin-bottom:8px}
.step p{font-size:13px;color:#9ca3af;line-height:1.5}
.pricing-card{max-width:400px;margin:0 auto;background:#1a1f2e;border:1px solid rgba(16,185,129,.3);border-radius:16px;padding:32px;text-align:center}
.pricing-label{color:#34d399;font-size:14px;font-weight:600;margin-bottom:8px}
.pricing-amount{font-size:48px;font-weight:700;color:#fff}
.pricing-period{color:#9ca3af;font-size:16px}
.pricing-features{list-style:none;margin:24px 0;text-align:left}
.pricing-features li{padding:8px 0;font-size:14px;color:#d1d5db;display:flex;align-items:center;gap:10px}
.pricing-features li::before{content:"✓";color:#34d399;font-weight:700}
.pricing-note{font-size:12px;color:#6b7280;margin-top:12px}
.cta-section{text-align:center;padding:80px 24px;border-top:1px solid #1a1f2e}
.cta-section h2{font-size:32px;font-weight:700;color:#fff;margin-bottom:12px}
.cta-section p{color:#9ca3af;margin-bottom:24px;font-size:16px}
footer{border-top:1px solid #2a3040;padding:20px 24px;text-align:center}
.footer-inner{max-width:1100px;margin:0 auto;display:flex;justify-content:space-between;align-items:center;font-size:12px;color:#6b7280;flex-wrap:wrap;gap:12px}
.footer-links{display:flex;gap:16px}
.footer-links a{color:#6b7280;font-size:12px}
.content-body{max-width:700px;margin:0 auto;padding:0 24px 60px}
.content-body h2{color:#fff;font-size:22px;font-weight:600;margin:32px 0 12px}
.content-body h3{color:#e5e7eb;font-size:18px;font-weight:600;margin:24px 0 8px}
.content-body p{margin-bottom:16px;line-height:1.7;font-size:15px}
.content-body ul,.content-body ol{margin:0 0 16px 24px}
.content-body li{margin-bottom:8px;font-size:15px;line-height:1.6}
.compare-table{width:100%;max-width:600px;margin:0 auto 40px;border-collapse:collapse}
.compare-table th,.compare-table td{padding:14px 16px;text-align:left;border-bottom:1px solid #2a3040;font-size:14px}
.compare-table th{color:#9ca3af;font-weight:500;font-size:13px}
.compare-table td{color:#d1d5db}
.compare-table .highlight{color:#34d399;font-weight:600}
.compare-table thead th:first-child{color:transparent}
.internal-links{max-width:900px;margin:0 auto;padding:0 24px}
.internal-links h3{color:#fff;font-size:16px;font-weight:600;margin-bottom:12px}
.link-grid{display:flex;flex-wrap:wrap;gap:8px;margin-bottom:32px}
.link-grid a{display:inline-block;padding:6px 14px;background:#1a1f2e;border:1px solid #2a3040;border-radius:8px;font-size:12px;color:#9ca3af;transition:border-color .2s}
.link-grid a:hover{border-color:#34d399;color:#34d399}
</style>
</head>
<body>
<nav class="nav"><div class="nav-inner">
<a href="/" class="logo"><div class="logo-icon">R</div>ReplyIQ</a>
<div class="nav-links"><a href="/login">Log in</a><a href="/signup" class="btn">Start Free Trial</a></div>
</div></nav>
${body}
<footer><div class="footer-inner">
<span>&copy; 2026 Erie Apps LLC</span>
<div class="footer-links"><a href="/terms">Terms</a><a href="/privacy">Privacy</a></div>
</div></footer>
</body>
</html>`
}

function howItWorks() {
  return `<div class="section"><h2 class="section-title">How it works</h2>
<div class="steps">
<div class="step"><div class="step-num">1</div><h3>Connect your Google Business Profile</h3><p>One-click OAuth connection. Takes 2 minutes. We pull in all your existing reviews immediately.</p></div>
<div class="step"><div class="step-num">2</div><h3>AI generates human-sounding replies</h3><p>Every reply is unique — warm for 5-star reviews, professional for critical ones. Never sounds robotic.</p></div>
<div class="step"><div class="step-num">3</div><h3>Replies post automatically</h3><p>Choose auto-post for hands-free operation, or review each reply before it goes live.</p></div>
</div></div>`
}

function pricingSection() {
  return `<div class="section"><h2 class="section-title">Simple pricing</h2>
<div class="pricing-card">
<div class="pricing-label">ReplyIQ Pro</div>
<div><span class="pricing-amount">$19.99</span><span class="pricing-period"> /month</span></div>
<ul class="pricing-features">
<li>Unlimited review monitoring</li>
<li>AI-generated replies for every review</li>
<li>Auto-post or approve-first mode</li>
<li>Multiple locations supported</li>
<li>Tone customization per location</li>
<li>Dashboard with analytics</li>
</ul>
<a href="/signup" class="btn btn-lg" style="display:block">Start 7-Day Free Trial</a>
<p class="pricing-note">No credit card required</p>
</div></div>`
}

function ctaSection(heading, sub) {
  return `<div class="cta-section"><h2>${heading}</h2><p>${sub}</p><a href="/signup" class="btn btn-lg">Start Free Trial</a></div>`
}

function internalLinks(currentType, currentSlug) {
  let html = '<div class="section"><div class="internal-links">'

  if (currentType !== 'category') {
    html += '<h3>By Business Type</h3><div class="link-grid">'
    categories.forEach(c => { html += `<a href="/for/${c.slug}">${c.name}</a>` })
    html += '</div>'
  }

  if (currentType !== 'city') {
    html += '<h3>By City</h3><div class="link-grid">'
    cities.forEach(c => { html += `<a href="/in/${c.slug}">${c.name}</a>` })
    html += '</div>'
  }

  html += '</div></div>'
  return html
}

// Generate category pages
function generateCategoryPages() {
  const dir = path.join(outputDir, 'for')
  ensureDir(dir)

  categories.forEach(cat => {
    const pageDir = path.join(dir, cat.slug)
    ensureDir(pageDir)
    const html = layout({
      title: `Google Review Management for ${cat.name} — ReplyIQ`,
      metaDescription: `ReplyIQ automatically responds to Google reviews for ${cat.name.toLowerCase()}. Set it up in 2 minutes. $19.99/month. 7-day free trial.`,
      canonicalPath: `/for/${cat.slug}`,
      body: `
        <div class="hero">
          <h1>Google Review Management for ${cat.name}</h1>
          <p>${cat.description}</p>
          <a href="/signup" class="btn btn-lg">Start Free Trial</a>
        </div>
        ${howItWorks()}
        ${pricingSection()}
        ${internalLinks('category', cat.slug)}
        ${ctaSection(`Start managing reviews for your ${cat.name.toLowerCase().replace(/s$/, '')} today`, 'Every unanswered review is a missed opportunity.')}
      `,
    })
    fs.writeFileSync(path.join(pageDir, 'index.html'), html)
  })

  console.log(`  Generated ${categories.length} category pages`)
}

// Generate city pages
function generateCityPages() {
  const dir = path.join(outputDir, 'in')
  ensureDir(dir)

  cities.forEach(city => {
    const pageDir = path.join(dir, city.slug)
    ensureDir(pageDir)
    const cityDesc = city.description

    const html = layout({
      title: `Google Review Management in ${city.name} — ReplyIQ`,
      metaDescription: `ReplyIQ helps ${city.name} businesses automatically respond to Google reviews. $19.99/month. 7-day free trial.`,
      canonicalPath: `/in/${city.slug}`,
      body: `
        <div class="hero">
          <h1>Google Review Management for ${city.name} Businesses</h1>
          <p>${cityDesc}</p>
          <a href="/signup" class="btn btn-lg">Start Free Trial</a>
        </div>
        ${howItWorks()}
        ${pricingSection()}
        ${internalLinks('city', city.slug)}
        ${ctaSection(`${city.name} businesses — start responding to reviews today`, 'Set up in 2 minutes. No credit card required.')}
      `,
    })
    fs.writeFileSync(path.join(pageDir, 'index.html'), html)
  })

  console.log(`  Generated ${cities.length} city pages`)
}

// Generate comparison pages
function generateComparisonPages() {
  const dir = path.join(outputDir, 'vs')
  ensureDir(dir)

  competitors.forEach(comp => {
    const pageDir = path.join(dir, comp.slug)
    ensureDir(pageDir)
    const compDesc = `${comp.name} is a review management platform — but it's built for enterprises, not small business owners. ReplyIQ does one thing and does it well: automatically responds to your Google reviews using AI. No bloated feature set, no sales calls, no enterprise pricing. Just plug it in and your reviews get handled.`

    const html = layout({
      title: `ReplyIQ vs ${comp.name} — Which is Better for Review Management?`,
      metaDescription: `Compare ReplyIQ and ${comp.name} for Google review management. See pricing, features, and setup time side by side.`,
      canonicalPath: `/vs/${comp.slug}`,
      body: `
        <div class="hero">
          <h1>ReplyIQ vs ${comp.name}</h1>
          <p>${compDesc}</p>
        </div>
        <div class="section">
          <h2 class="section-title">Feature Comparison</h2>
          <table class="compare-table">
            <thead><tr><th></th><th class="highlight">ReplyIQ</th><th>${comp.name}</th></tr></thead>
            <tbody>
              <tr><td>Price</td><td class="highlight">$19.99/mo</td><td>${comp.price}</td></tr>
              <tr><td>AI Auto-Posting</td><td class="highlight">Yes</td><td>${comp.autoPost}</td></tr>
              <tr><td>Setup Time</td><td class="highlight">2 minutes</td><td>${comp.setup}</td></tr>
              <tr><td>Free Trial</td><td class="highlight">7 days, no card</td><td>${comp.trial}</td></tr>
              <tr><td>AI Reply Generation</td><td class="highlight">Yes — every review</td><td>Limited or manual</td></tr>
              <tr><td>Built for</td><td class="highlight">Small businesses</td><td>Mid-market / Enterprise</td></tr>
            </tbody>
          </table>
          <div style="text-align:center"><a href="/signup" class="btn btn-lg">Try ReplyIQ Free</a></div>
        </div>
        ${internalLinks('comparison', comp.slug)}
        ${ctaSection('Simple beats complicated', `Skip the demo calls and enterprise pricing. Start your free trial now.`)}
      `,
    })
    fs.writeFileSync(path.join(pageDir, 'index.html'), html)
  })

  console.log(`  Generated ${competitors.length} comparison pages`)
}

// Generate how-to pages
function generateHowToPages() {
  const dir = path.join(outputDir, 'how-to')
  ensureDir(dir)

  howtos.forEach(article => {
    const pageDir = path.join(dir, article.slug)
    ensureDir(pageDir)
    const html = layout({
      title: `${article.title} — ReplyIQ`,
      metaDescription: article.metaDescription,
      canonicalPath: `/how-to/${article.slug}`,
      body: `
        <div class="hero" style="padding-bottom:20px">
          <h1 style="font-size:clamp(24px,4vw,36px)">${article.title}</h1>
        </div>
        <div class="content-body">
          ${article.content}
        </div>
        ${ctaSection('Let ReplyIQ handle your reviews', 'Automatic AI-powered responses. $19.99/month. 7-day free trial.')}
        ${internalLinks('howto', article.slug)}
      `,
    })
    fs.writeFileSync(path.join(pageDir, 'index.html'), html)
  })

  console.log(`  Generated ${howtos.length} how-to pages`)
}

// Generate category + city combo pages
function generateComboPages() {
  const dir = path.join(outputDir, 'for')
  let count = 0

  categories.forEach(cat => {
    cities.forEach(city => {
      const pageDir = path.join(dir, cat.slug, 'in', city.slug)
      ensureDir(pageDir)

      const comboDesc = `${cat.name} in ${city.name}, ${city.state} face fierce competition for customers — and Google reviews are the battleground. When someone searches for a ${cat.name.toLowerCase().replace(/s$/, '')} in ${city.name}, they see star ratings and review responses before they see your menu, your prices, or your hours. ReplyIQ helps ${city.name} ${cat.name.toLowerCase()} stay on top of every review automatically.`

      const html = layout({
        title: `Google Review Management for ${cat.name} in ${city.name} — ReplyIQ`,
        metaDescription: `ReplyIQ automatically responds to Google reviews for ${cat.name.toLowerCase()} in ${city.name}, ${city.state}. $19.99/month. 7-day free trial.`,
        canonicalPath: `/for/${cat.slug}/in/${city.slug}`,
        body: `
          <div class="hero">
            <h1>Google Review Management for ${cat.name} in ${city.name}</h1>
            <p>${comboDesc}</p>
            <a href="/signup" class="btn btn-lg">Start Free Trial</a>
          </div>
          ${howItWorks()}
          ${pricingSection()}
          <div class="section"><div class="internal-links">
            <h3>More ${cat.name} Pages</h3><div class="link-grid">
            ${cities.filter(c => c.slug !== city.slug).slice(0, 10).map(c => `<a href="/for/${cat.slug}/in/${c.slug}">${c.name}</a>`).join('')}
            </div>
            <h3>More ${city.name} Business Types</h3><div class="link-grid">
            ${categories.filter(c => c.slug !== cat.slug).slice(0, 10).map(c => `<a href="/for/${c.slug}/in/${city.slug}">${c.name}</a>`).join('')}
            </div>
            <h3>Browse All</h3><div class="link-grid">
            <a href="/for/${cat.slug}">${cat.name}</a>
            <a href="/in/${city.slug}">${city.name}</a>
            </div>
          </div></div>
          ${ctaSection(`${city.name} ${cat.name.toLowerCase()} — start responding to reviews today`, 'Set up in 2 minutes. No credit card required.')}
        `,
      })
      fs.writeFileSync(path.join(pageDir, 'index.html'), html)
      count++
    })
  })

  console.log(`  Generated ${count} category+city combo pages`)
}

// Generate sitemap.xml
function generateSitemap() {
  const urls = [
    { path: '/', priority: '1.0' },
    { path: '/signup', priority: '0.9' },
    { path: '/login', priority: '0.5' },
    { path: '/terms', priority: '0.3' },
    { path: '/privacy', priority: '0.3' },
  ]

  categories.forEach(c => urls.push({ path: `/for/${c.slug}`, priority: '0.8' }))
  cities.forEach(c => urls.push({ path: `/in/${c.slug}`, priority: '0.8' }))
  categories.forEach(cat => {
    cities.forEach(city => {
      urls.push({ path: `/for/${cat.slug}/in/${city.slug}`, priority: '0.6' })
    })
  })
  competitors.forEach(c => urls.push({ path: `/vs/${c.slug}`, priority: '0.7' }))
  howtos.forEach(h => urls.push({ path: `/how-to/${h.slug}`, priority: '0.7' }))

  const xml = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${urls.map(u => `  <url>
    <loc>${SITE_URL}${u.path}</loc>
    <priority>${u.priority}</priority>
    <changefreq>weekly</changefreq>
  </url>`).join('\n')}
</urlset>`

  fs.writeFileSync(path.join(outputDir, 'sitemap.xml'), xml)
  console.log(`  Generated sitemap.xml with ${urls.length} URLs`)
}

// Generate robots.txt
function generateRobotsTxt() {
  const robots = `User-agent: *
Allow: /

Sitemap: ${SITE_URL}/sitemap.xml`

  fs.writeFileSync(path.join(outputDir, 'robots.txt'), robots)
  console.log(`  Generated robots.txt`)
}

// Run everything
console.log('\nGenerating SEO pages...\n')
generateCategoryPages()
generateCityPages()
generateComboPages()
generateComparisonPages()
generateHowToPages()
generateSitemap()
generateRobotsTxt()

const combos = categories.length * cities.length
const total = categories.length + cities.length + combos + competitors.length + howtos.length
console.log(`\nDone! ${total} SEO pages + sitemap.xml + robots.txt generated.\n`)
