import { useState } from 'react';
import DataTable from './components/DataTable';
import OrderUseCase from './components/OrderUseCase';
import './App.css';

const ENDPOINTS = [
    { id: 'clients', name: 'Klienci', url: '/api/clients' },
    { id: 'furniture', name: 'Meble', url: '/api/furniture' },
    { id: 'branches', name: 'Placówki', url: '/api/branches' },
    { id: 'employees', name: 'Pracownicy', url: '/api/employees' },
    { id: 'orders', name: 'Zamówienia', url: '/api/orders'}
];

function App() {
    const [activeTab, setActiveTab] = useState(ENDPOINTS[0]);
    const [view, setView] = useState('tables');

    function selectEndpoint(endpoint) {
        setActiveTab(endpoint);
        setView('tables');
    }

    if (view === 'use-case') {
        return <OrderUseCase onBack={() => setView('tables')} />;
    }

    return (
        <div className="dashboard">
            <header className="sidebar">
                <h1>Szwedki Sklep</h1>
                <nav>
                    {ENDPOINTS.map(endpoint => (
                        <button
                            key={endpoint.id}
                            className={view === 'tables' && activeTab.id === endpoint.id ? 'active' : ''}
                            onClick={() => selectEndpoint(endpoint)}
                        >
                            {endpoint.name}
                        </button>
                    ))}
                </nav>
            </header>

            <main className="content">
                <div className="content-header">
                    <div>
                        <p className="eyebrow">Panel główny</p>
                        <h2>{activeTab.name}</h2>
                    </div>
                    <button
                        type="button"
                        className="primary-action"
                        onClick={() => setView('use-case')}
                    >
                        Uruchom przypadek użycia
                    </button>
                </div>

                <DataTable
                    key={activeTab.id}
                    title={activeTab.name}
                    endpoint={activeTab.url}
                />
            </main>
        </div>
    );
}

export default App;
