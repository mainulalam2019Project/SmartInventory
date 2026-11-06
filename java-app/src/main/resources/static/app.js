const API = '/api/v1';
let token = localStorage.getItem('token');
let currentUser = null;

const $ = (s) => document.querySelector(s);
const $$ = (s) => document.querySelectorAll(s);

function headers() { return token ? { 'Authorization': 'Bearer ' + token } : {}; }

async function api(path, opts = {}) {
  const res = await fetch(API + path, { ...opts, headers: { ...headers(), ...(opts.headers || {}) } });
  if (res.status === 401) { logout(); throw new Error('Unauthorized'); }
  const json = await res.json();
  if (!json.success) throw new Error(json.error?.message || 'Request failed');
  return json.data;
}

function logout() {
  token = null; currentUser = null;
  localStorage.removeItem('token');
  $('#loginView').classList.remove('hidden');
  $('#appView').classList.add('hidden');
}

async function login(u, p) {
  const res = await fetch(API + '/auth/login', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: u, password: p })
  });
  const json = await res.json();
  if (!json.success) throw new Error(json.error?.message || 'Login failed');
  token = json.data.token;
  currentUser = json.data.user;
  localStorage.setItem('token', token);
  $('#loginView').classList.add('hidden');
  $('#appView').classList.remove('hidden');
  $('#userInfo').textContent = currentUser.username + ' (' + currentUser.roleName + ')';
  showView('dashboard');
}

$('#loginForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  $('#loginError').textContent = '';
  try {
    await login($('#username').value, $('#password').value);
  } catch (err) {
    $('#loginError').textContent = err.message;
  }
});

$('#logoutBtn').addEventListener('click', logout);

$$('.sidebar nav a').forEach(a => {
  a.addEventListener('click', (e) => {
    e.preventDefault();
    $$('.sidebar nav a').forEach(x => x.classList.remove('active'));
    a.classList.add('active');
    showView(a.dataset.view);
  });
});

const titles = { dashboard:'Dashboard', products:'Products', inventory:'Inventory', warehouses:'Warehouses',
  suppliers:'Suppliers', orders:'Sales Orders', purchase:'Purchase Orders', transfers:'Stock Transfers',
  alerts:'Low Stock Alerts', analytics:'Analytics' };

function showView(name) {
  $('#viewTitle').textContent = titles[name] || name;
  const c = $('#viewContainer');
  c.innerHTML = '<p class="empty">Loading...</p>';
  const renderers = { dashboard: renderDashboard, products: renderProducts, inventory: renderInventory,
    warehouses: renderWarehouses, suppliers: renderSuppliers, orders: renderSalesOrders, purchase: renderPurchaseOrders,
    transfers: renderTransfers, alerts: renderAlerts, analytics: renderAnalytics };
  (renderers[name] || (() => { c.innerHTML = '<p class="empty">Not implemented</p>'; }))();
}

// ---------- Dashboard ----------
async function renderDashboard() {
  const c = $('#viewContainer');
  c.innerHTML = `
    <div class="kpi-grid" id="kpis"></div>
    <div class="chart-card"><canvas id="stockChart" height="120"></canvas></div>
    <div class="chart-card"><canvas id="salesChart" height="120"></canvas></div>`;
  try {
    const [inv, products, alerts, orders] = await Promise.all([
      api('/inventory'), api('/products'), api('/alerts'), api('/sales-orders')
    ]);
    const totalOnHand = inv.reduce((s, i) => s + (i.quantityOnHand || 0), 0);
    const lowStock = alerts.filter(a => a.status === 'OPEN').length;
    const totalProducts = products.length;
    const totalOrders = orders.length;
    $('#kpis').innerHTML = `
      <div class="kpi"><div class="label">Total Products</div><div class="value">${totalProducts}</div></div>
      <div class="kpi"><div class="label">Total On Hand</div><div class="value">${totalOnHand}</div></div>
      <div class="kpi"><div class="label">Open Alerts</div><div class="value">${lowStock}</div></div>
      <div class="kpi"><div class="label">Sales Orders</div><div class="value">${totalOrders}</div></div>`;
    // stock by warehouse chart
    const whMap = {};
    inv.forEach(i => { whMap[i.warehouseCode] = (whMap[i.warehouseCode] || 0) + i.quantityOnHand; });
    new Chart($('#stockChart'), { type:'bar', data:{ labels:Object.keys(whMap), datasets:[{ label:'On Hand', data:Object.values(whMap), backgroundColor:'#38bdf8' }] }, options:{ responsive:true, plugins:{ title:{ display:true, text:'Stock by Warehouse', color:'#e2e8f0' } } } });
    // sales status chart
    const stMap = {};
    orders.forEach(o => { stMap[o.status] = (stMap[o.status] || 0) + 1; });
    new Chart($('#salesChart'), { type:'doughnut', data:{ labels:Object.keys(stMap), datasets:[{ data:Object.values(stMap), backgroundColor:['#38bdf8','#34d399','#fbbf24','#f87171','#a78bfa'] }] }, options:{ responsive:true, plugins:{ title:{ display:true, text:'Sales Orders by Status', color:'#e2e8f0' } } } });
  } catch (e) { c.innerHTML = '<p class="empty">Error: ' + e.message + '</p>'; }
}

// ---------- Products ----------
async function renderProducts() {
  const c = $('#viewContainer');
  let products = [];
  try { products = await api('/products'); } catch (e) { c.innerHTML = '<p class="empty">' + e.message + '</p>'; return; }
  c.innerHTML = `
    <div class="form-row">
      <input id="pSku" placeholder="SKU" />
      <input id="pName" placeholder="Name" />
      <input id="pCost" placeholder="Cost" type="number" step="0.01" />
      <input id="pSell" placeholder="Sell" type="number" step="0.01" />
      <input id="pReorder" placeholder="Reorder" type="number" />
      <button class="btn" id="addProductBtn">Add Product</button>
    </div>
    <table><thead><tr><th>ID</th><th>SKU</th><th>Name</th><th>Category</th><th>Cost</th><th>Sell</th><th>Reorder</th><th>Status</th></tr></thead>
    <tbody>${products.map(p => `<tr><td>${p.id}</td><td>${p.sku}</td><td>${p.name}</td><td>${p.categoryName||'-'}</td><td>${p.costPrice}</td><td>${p.sellingPrice}</td><td>${p.reorderPoint}</td><td>${p.status}</td></tr>`).join('')}</tbody></table>`;
  $('#addProductBtn').addEventListener('click', async () => {
    try {
      await api('/products', { method:'POST', headers:{'Content-Type':'application/json'},
        body: JSON.stringify({ sku:$('#pSku').value, name:$('#pName').value, costPrice:parseFloat($('#pCost').value)||0, sellingPrice:parseFloat($('#pSell').value)||0, reorderPoint:parseInt($('#pReorder').value)||0, status:'ACTIVE', unit:'pcs' }) });
      renderProducts();
    } catch (e) { alert(e.message); }
  });
}

// ---------- Inventory ----------
async function renderInventory() {
  const c = $('#viewContainer');
  let inv = [];
  try { inv = await api('/inventory'); } catch (e) { c.innerHTML = '<p class="empty">' + e.message + '</p>'; return; }
  c.innerHTML = `
    <table><thead><tr><th>ID</th><th>Product</th><th>SKU</th><th>Warehouse</th><th>On Hand</th><th>Reserved</th><th>Available</th><th>In Transit</th></tr></thead>
    <tbody>${inv.map(i => `<tr><td>${i.id}</td><td>${i.productName}</td><td>${i.sku}</td><td>${i.warehouseCode}</td><td>${i.quantityOnHand}</td><td>${i.quantityReserved}</td><td>${i.quantityOnHand-i.quantityReserved}</td><td>${i.quantityInTransit}</td></tr>`).join('')}</tbody></table>`;
}

// ---------- Warehouses ----------
async function renderWarehouses() {
  const c = $('#viewContainer');
  let wh = [];
  try { wh = await api('/warehouses'); } catch (e) { c.innerHTML = '<p class="empty">' + e.message + '</p>'; return; }
  c.innerHTML = `<table><thead><tr><th>ID</th><th>Code</th><th>Name</th><th>City</th><th>Country</th><th>Capacity</th><th>Active</th></tr></thead>
    <tbody>${wh.map(w => `<tr><td>${w.id}</td><td>${w.code}</td><td>${w.name}</td><td>${w.city||'-'}</td><td>${w.country||'-'}</td><td>${w.capacity}</td><td>${w.active}</td></tr>`).join('')}</tbody></table>`;
}

// ---------- Suppliers ----------
async function renderSuppliers() {
  const c = $('#viewContainer');
  let s = [];
  try { s = await api('/suppliers'); } catch (e) { c.innerHTML = '<p class="empty">' + e.message + '</p>'; return; }
  c.innerHTML = `<table><thead><tr><th>ID</th><th>Name</th><th>Contact</th><th>Email</th><th>City</th><th>Lead Time</th><th>Rating</th></tr></thead>
    <tbody>${s.map(x => `<tr><td>${x.id}</td><td>${x.name}</td><td>${x.contactPerson||'-'}</td><td>${x.email||'-'}</td><td>${x.city||'-'}</td><td>${x.leadTimeDays}</td><td>${x.rating||'-'}</td></tr>`).join('')}</tbody></table>`;
}

// ---------- Sales Orders ----------
async function renderSalesOrders() {
  const c = $('#viewContainer');
  let o = [];
  try { o = await api('/sales-orders'); } catch (e) { c.innerHTML = '<p class="empty">' + e.message + '</p>'; return; }
  c.innerHTML = `<table><thead><tr><th>ID</th><th>Number</th><th>Customer</th><th>Warehouse</th><th>Status</th><th>Total</th><th>Date</th></tr></thead>
    <tbody>${o.map(x => `<tr><td>${x.id}</td><td>${x.orderNumber}</td><td>${x.customerName}</td><td>${x.warehouseCode}</td><td>${x.status}</td><td>${x.totalAmount}</td><td>${x.orderDate}</td></tr>`).join('')}</tbody></table>`;
}

// ---------- Purchase Orders ----------
async function renderPurchaseOrders() {
  const c = $('#viewContainer');
  let o = [];
  try { o = await api('/purchase-orders'); } catch (e) { c.innerHTML = '<p class="empty">' + e.message + '</p>'; return; }
  c.innerHTML = `<table><thead><tr><th>ID</th><th>Number</th><th>Supplier</th><th>Warehouse</th><th>Status</th><th>Total</th><th>Date</th></tr></thead>
    <tbody>${o.map(x => `<tr><td>${x.id}</td><td>${x.poNumber}</td><td>${x.supplierName}</td><td>${x.warehouseCode}</td><td>${x.status}</td><td>${x.totalAmount}</td><td>${x.orderDate}</td></tr>`).join('')}</tbody></table>`;
}

// ---------- Transfers ----------
async function renderTransfers() {
  const c = $('#viewContainer');
  let t = [];
  try { t = await api('/transfers'); } catch (e) { c.innerHTML = '<p class="empty">' + e.message + '</p>'; return; }
  c.innerHTML = `<table><thead><tr><th>ID</th><th>Number</th><th>From</th><th>To</th><th>Status</th><th>Shipped</th></tr></thead>
    <tbody>${t.map(x => `<tr><td>${x.id}</td><td>${x.transferNumber}</td><td>${x.fromWarehouseCode}</td><td>${x.toWarehouseCode}</td><td>${x.status}</td><td>${x.shippedAt||'-'}</td></tr>`).join('')}</tbody></table>`;
}

// ---------- Alerts ----------
async function renderAlerts() {
  const c = $('#viewContainer');
  let a = [];
  try { a = await api('/alerts'); } catch (e) { c.innerHTML = '<p class="empty">' + e.message + '</p>'; return; }
  c.innerHTML = `<table><thead><tr><th>ID</th><th>Product</th><th>SKU</th><th>Warehouse</th><th>Current</th><th>Threshold</th><th>Status</th><th>Action</th></tr></thead>
    <tbody>${a.map(x => `<tr><td>${x.id}</td><td>${x.productName}</td><td>${x.sku}</td><td>${x.warehouseCode}</td><td>${x.currentQty}</td><td>${x.threshold}</td><td><span class="badge ${x.status.toLowerCase()}">${x.status}</span></td><td>${x.status==='OPEN'?`<button class="btn secondary" onclick="resolveAlert(${x.id})">Resolve</button>`:''}</td></tr>`).join('')}</tbody></table>`;
}
window.resolveAlert = async (id) => { try { await api('/alerts/'+id+'/resolve', {method:'POST'}); renderAlerts(); } catch(e){ alert(e.message);} };

// ---------- Analytics ----------
async function renderAnalytics() {
  const c = $('#viewContainer');
  c.innerHTML = `
    <div class="chart-card"><h3>Run Inventory Performance (via Python)</h3>
      <p class="empty">Send current inventory snapshot to Python analytics. (Demo: uses first 5 inventory rows)</p>
      <button class="btn" id="runPerf">Run Analysis</button>
      <pre id="perfOut" style="margin-top:1rem; white-space:pre-wrap; font-size:.8rem;"></pre>
    </div>`;
  $('#runPerf').addEventListener('click', async () => {
    try {
      const inv = await api('/inventory');
      const payload = { inventory: inv.slice(0,5).map(i => ({ product_id: i.productId, product_name: i.productName, quantity_on_hand: i.quantityOnHand, quantity_reserved: i.quantityReserved, reorder_point: 10, unit_cost: 10 })), sales: [], period_days: 30 };
      const res = await api('/analytics/inventory/performance', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) });
      $('#perfOut').textContent = JSON.stringify(res.summary, null, 2);
    } catch (e) { $('#perfOut').textContent = 'Error: ' + e.message; }
  });
}

// Auto-login if token exists
if (token) {
  $('#loginView').classList.add('hidden');
  $('#appView').classList.remove('hidden');
  $('#userInfo').textContent = 'Logged in';
  showView('dashboard');
}
EOF