import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Dashboard.css";

function Dashboard() {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const warehouseName = localStorage.getItem("warehouseName");
    const authToken = localStorage.getItem("authToken");
    const apiUrl = localStorage.getItem("apiUrl");  // ← Get API URL

    useEffect(() => {
        if (!authToken || !apiUrl) {
            navigate("/");
            return;
        }
        fetchOrders();
    }, []);

    async function fetchOrders() {
        setLoading(true);
        setError("");
        
        try {
            const response = await fetch(`${apiUrl}/api/warehouse/orders`, {
                headers: {
                    "Authorization": `Bearer ${authToken}`,
                },
            });

            if (response.ok) {
                const data = await response.json();
                setOrders(data);
            } else if (response.status === 401) {
                localStorage.clear();
                navigate("/");
            } else {
                setError("Failed to fetch orders");
            }
        } catch (err) {
            setError("Failed to connect to warehouse");
        } finally {
            setLoading(false);
        }
    }

    async function handleProceed(orderId) {
        if (!window.confirm("Are you sure you want to fulfill this order?")) {
            return;
        }

        try {
            const response = await fetch(
                `${apiUrl}/api/warehouse/orders/${orderId}/fulfill`,
                {
                    method: "POST",
                    headers: {
                        "Authorization": `Bearer ${authToken}`,
                    },
                }
            );

            if (response.ok) {
                setOrders(orders.filter(order => order.orderId !== orderId));
                alert("✅ Order fulfilled successfully!");
            } else {
                const text = await response.text();
                alert("❌ Failed to fulfill order: " + text);
            }
        } catch (err) {
            alert("❌ Error: " + err.message);
        }
    }

    function handleLogout() {
        localStorage.clear();
        navigate("/");
    }

    return (
        <div className="dashboard-container">
            <header className="dashboard-header">
                <h1>{warehouseName} - Order Dashboard</h1>
                <button className="logout-button" onClick={handleLogout}>
                    Logout
                </button>
            </header>

            {loading && <div className="loading">Loading orders...</div>}
            {error && <div className="error-message">{error}</div>}

            <div className="orders-grid">
                {orders.length === 0 && !loading && !error && (
                    <div className="no-orders">
                        <p>📭 No pending orders</p>
                        <button onClick={fetchOrders} className="refresh-button">
                            Refresh
                        </button>
                    </div>
                )}

                {orders.map((order) => (
                    <div key={order.orderId} className="order-card">
                        <div className="order-header">
                            <h3>#{order.orderId}</h3>
                            <span className="order-badge">PENDING</span>
                        </div>
                        <div className="order-details">
                            <div className="detail-row">
                                <span className="label">Product:</span>
                                <span className="value">{order.productName}</span>
                            </div>
                            <div className="detail-row">
                                <span className="label">Quantity:</span>
                                <span className="value">{order.quantity} units</span>
                            </div>
                            <div className="detail-row">
                                <span className="label">Retailer ID:</span>
                                <span className="value">#{order.retailerId}</span>
                            </div>
                            <div className="detail-row">
                                <span className="label">Received:</span>
                                <span className="value">
                                    {new Date(order.receivedAt).toLocaleString()}
                                </span>
                            </div>
                        </div>
                        <button
                            className="proceed-button"
                            onClick={() => handleProceed(order.orderId)}
                        >
                            ✓ Proceed & Fulfill
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default Dashboard;