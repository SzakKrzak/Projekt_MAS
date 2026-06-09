export const API_BASE_URL = 'http://localhost:8080';

export async function apiRequest(endpoint, options = {}) {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);

    if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status}`);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function jsonRequest(method, body) {
    return {
        method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
    };
}

export const api = {
    getClients: () => apiRequest('/api/clients'),
    getFurniture: () => apiRequest('/api/furniture'),
    getEmployees: () => apiRequest('/api/employees'),
    getBranches: () => apiRequest('/api/branches'),
    createOrder: (order) => apiRequest('/api/orders', jsonRequest('POST', order)),
    addOrderLine: (orderId, line) => apiRequest(`/api/orders/${orderId}/lines`, jsonRequest('POST', line)),
    payOrder: (orderId, deliveryDeadline) => apiRequest(`/api/orders/${orderId}/pay`, jsonRequest('POST', { deliveryDeadline })),
    getOrderConfig: () => apiRequest('/api/config/orders'),
};
