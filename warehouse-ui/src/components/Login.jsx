import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Login.css";

function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [warehousePort, setWarehousePort] = useState("8081");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");
        setLoading(true);

        const API_URL = `http://localhost:${warehousePort}`;

        try {
            const response = await fetch(`${API_URL}/api/auth/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ username, password }),
            });

            const data = await response.json();

            if (response.ok && data.token) {
                // Save everything including API URL
                localStorage.setItem("authToken", data.token);
                localStorage.setItem("warehouseId", data.warehouseId);
                localStorage.setItem("warehouseName", data.warehouseName);
                localStorage.setItem("apiUrl", API_URL);  // ← Save API URL

                navigate("/dashboard");
            } else {
                setError(data.message || "Invalid credentials");
            }
        } catch (err) {
            setError("Failed to connect to server. Is the warehouse running?");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="login-container">
            <h1>Warehouse Login</h1>
            <form className="login-form" onSubmit={handleSubmit}>
                {error && <div className="error-message">{error}</div>}
                
                
                {/* Warehouse Selection
                <select
                    className="login-input"
                    value={warehousePort}
                    onChange={(e) => setWarehousePort(e.target.value)}
                >
                    <option value="8081">Warehouse 1</option>
                    <option value="8091">Warehouse 2 </option>
                    <option value="8101">Warehouse 3 </option>
                </select> */}
                

                <input
                    type="text"
                    className="login-input"
                    placeholder="Username (e.g., warehouse1)"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
                <input
                    type="password"
                    className="login-input"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                <button 
                    className="login-button" 
                    type="submit"
                    disabled={loading}
                >
                    {loading ? "Logging in..." : "Login"}
                </button>
            </form>
        </div>
    );
}

export default Login;