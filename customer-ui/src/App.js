import { useEffect, useState } from "react";

function App() {
  const [customerName, setCustomerName] = useState("");
  const [loggedIn, setLoggedIn] = useState(false);
  const [products, setProducts] = useState([]);
  const [quantities, setQuantities] = useState({});

  // Load products only after login
  useEffect(() => {
    if (!loggedIn) return;

    fetch("http://localhost:8083/api/customer/products")
      .then(res => res.json())
      .then(data => setProducts(data))
      .catch(() => alert("Failed to load products"));
  }, [loggedIn]);

  const login = () => {
    if (!customerName.trim()) {
      alert("Please enter your name");
      return;
    }
    setLoggedIn(true);
  };

  const buyProduct = (productId) => {
    const qty = quantities[productId] || 1;
    fetch(
      `http://localhost:8083/api/customer/orders?productId=${productId}&quantity=${qty}&customerName=${customerName}`,
      { method: "POST" }
    )
      .then(res => res.json())
      .then(() => {
        alert("Order placed successfully");
        window.location.reload();
      })
      .catch(() => alert("Order failed"));
  };

  // 🔐 LOGIN PAGE
  if (!loggedIn) {
    return (
      <div style={{ padding: "40px" }}>
        <h2>Customer Login</h2>

        <input
          type="text"
          placeholder="Enter your name"
          value={customerName}
          onChange={e => setCustomerName(e.target.value)}
        />
        <br /><br />

        <button onClick={login}>Login</button>
      </div>
    );
  }

  // 🛒 SHOP PAGE
  return (
    <div style={{ padding: "20px" }}>
      <h2>Welcome, {customerName}</h2>

      {products.map(p => (
        <div
          key={p.id}
          style={{
            border: "1px solid #ccc",
            padding: "10px",
            marginBottom: "10px"
          }}
        >
          <b>{p.productName}</b>
          <p>Price: ₹{p.price}</p>
          <p>Available: {p.quantity}</p>

          <input
            type="number"
            min="1"
            value={quantities[p.id] || 1}
            onChange={e => setQuantities({ ...quantities, [p.id]: e.target.value })}
          />
          <br /><br />

          <button onClick={() => buyProduct(p.id)}>
            Buy
          </button>
        </div>
      ))}
    </div>
  );
}

export default App;