import {useEffect, useMemo, useState} from 'react';
import {api} from '../services/api';

const VIEW = {
    CATALOG: 'catalog',
    CART: 'cart',
    SUMMARY: 'summary',
    CONFIRMATION: 'confirmation',
};

const currency = new Intl.NumberFormat('pl-PL', {
    style: 'currency',
    currency: 'PLN',
});

const statusLabels = {
    CREATED: 'Utworzone',
    PAID: 'Opłacone',
    COMPLETED: 'Zrealizowane',
    CANCELLED: 'Anulowane',
};

function addDays(days) {
    const daysNumber = Number(days);

    if (!Number.isFinite(daysNumber)) {
        throw new Error('Nieprawidłowa liczba dni w konfiguracji zamówienia.');
    }

    const date = new Date();
    date.setDate(date.getDate() + daysNumber);
    return date.toISOString().slice(0, 10);
}

function randomItem(items, ignoredId) {
    const availableItems = items.length > 1
        ? items.filter(item => item.id !== ignoredId)
        : items;

    return availableItems[Math.floor(Math.random() * availableItems.length)] || null;
}

function clientName(client) {
    if (!client) return 'Brak klienta';
    if (client.type === 'COMPANY') return client.companyName;
    return `${client.firstName} ${client.lastName}`;
}

function orderTotal(order) {
    return (order?.lines || []).reduce(
        (sum, line) => sum + Number(line.unitPrice) * line.quantity,
        0
    );
}

function OrderUseCase({onBack}) {
    const [view, setView] = useState(VIEW.CATALOG);
    const [clients, setClients] = useState([]);
    const [furniture, setFurniture] = useState([]);
    const [orderConfig, setOrderConfig] = useState(null);
    const [selectedClientId, setSelectedClientId] = useState('');
    const [deliveryAddress, setDeliveryAddress] = useState('');
    const [paymentDeadline, setPaymentDeadline] = useState('');
    const [quantities, setQuantities] = useState({});
    const [createdOrder, setCreatedOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [paying, setPaying] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        let active = true;

        async function loadData() {
            try {
                const [loadedClients, loadedFurniture, loadedConfig] = await Promise.all([
                    api.getClients(),
                    api.getFurniture(),
                    api.getOrderConfig(),
                ]);

                if (!active) return;

                setClients(loadedClients);
                setFurniture(loadedFurniture);
                setOrderConfig(loadedConfig);
                setPaymentDeadline(addDays(loadedConfig.timeToPay));

                const client = randomItem(loadedClients);
                if (client) {
                    setSelectedClientId(String(client.id));
                    setDeliveryAddress(client.address || '');
                }
            } catch (err) {
                if (active) setError(err.message || 'Nie udało się załadować danych.');
            } finally {
                if (active) setLoading(false);
            }
        }

        loadData();

        return () => {
            active = false;
        };
    }, []);

    const selectedClient = clients.find(client => String(client.id) === selectedClientId);

    const cart = useMemo(() => furniture
        .map(item => ({
            furniture: item,
            furnitureId: item.id,
            quantity: Number(quantities[item.id] || 0),
        }))
        .filter(item => item.quantity > 0), [furniture, quantities]);

    const cartTotal = cart.reduce(
        (sum, item) => sum + Number(item.furniture.price) * item.quantity,
        0
    );
    const minimalOrderValue = Number(orderConfig?.minimalOrderValue ?? 0);
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);

    function goTo(nextView) {
        setError(null);
        setView(nextView);
    }

    function setNewClient() {
        const client = randomItem(clients, selectedClient?.id);
        if (!client) return;

        setSelectedClientId(String(client.id));
        setDeliveryAddress(client.address || '');
        setCreatedOrder(null);
        setError(null);
    }

    function updateQuantity(furnitureId, quantity) {
        const newQuantity = Math.max(0, Number(quantity) || 0);

        setQuantities(previous => {
            const next = {...previous};
            if (newQuantity === 0) {
                delete next[furnitureId];
            } else {
                next[furnitureId] = newQuantity;
            }
            return next;
        });

        setCreatedOrder(null);
        setError(null);
    }

    function clearCart() {
        setQuantities({});
        setCreatedOrder(null);
        setError(null);
    }

    function showError(message, targetView) {
        setError(message);
        if (targetView) setView(targetView);
        return false;
    }

    function validateOrder() {
        if (!orderConfig) return showError('Konfiguracja zamówień nie została załadowana.');
        if (cart.length === 0) return showError('Dodaj przynajmniej jeden mebel do koszyka.', VIEW.CART);
        if (cartTotal < minimalOrderValue) {
            return showError(`Minimalna wartość zamówienia to ${currency.format(minimalOrderValue)}.`, VIEW.CART);
        }
        if (!selectedClient) return showError('Nie udało się wybrać klienta dla zamówienia.', VIEW.CATALOG);
        if (!deliveryAddress.trim()) return showError('Podaj adres dostawy.', VIEW.SUMMARY);
        if (!paymentDeadline) return showError('Nie udało się ustawić terminu opłaty.', VIEW.SUMMARY);
        return true;
    }

    function openSummary() {
        if (validateOrder()) goTo(VIEW.SUMMARY);
    }

    async function createOrder() {
        if (!validateOrder()) return;

        setSubmitting(true);
        setError(null);

        try {
            let order = await api.createOrder({
                clientId: selectedClient.id,
                deliveryAddress: deliveryAddress.trim(),
                paymentDeadline,
            });

            for (const item of cart) {
                order = await api.addOrderLine(order.id, {
                    furnitureId: item.furnitureId,
                    quantity: item.quantity,
                });
            }

            setCreatedOrder(order);
            setView(VIEW.CONFIRMATION);
        } catch (err) {
            setError(err.message || 'Nie udało się utworzyć zamówienia.');
        } finally {
            setSubmitting(false);
        }
    }

    async function payOrder() {
        if (!createdOrder || !orderConfig) return;

        setPaying(true);
        setError(null);

        try {
            const paidOrder = await api.payOrder(
                createdOrder.id,
                createdOrder.paymentDeadline
            );

            setCreatedOrder(paidOrder);
            setQuantities({});
        } catch (err) {
            setError(err.message || 'Nie udało się opłacić zamówienia.');
        } finally {
            setPaying(false);
        }
    }

    function startNewOrder() {
        setCreatedOrder(null);
        setPaymentDeadline(addDays(orderConfig.timeToPay));
        setNewClient();
        clearCart();
        setView(VIEW.CATALOG);
    }

    if (loading) {
        return (
            <section className="use-case-page">
                <p className="status loading">Ładowanie przypadku użycia...</p>
            </section>
        );
    }

    if (error && !orderConfig) {
        return (
            <section className="use-case-page">
                <PageHeader onBack={onBack}/>
                <p className="status error">{error}</p>
            </section>
        );
    }

    return (
        <section className="use-case-page">
            <PageHeader onBack={onBack}/>

            <nav className="use-case-steps" aria-label="Etapy przypadku użycia">
                <button type="button" className={view === VIEW.CATALOG ? 'current' : ''}
                        onClick={() => goTo(VIEW.CATALOG)}>
                    Katalog
                </button>
                <button type="button" className={view === VIEW.CART ? 'current' : ''}
                        onClick={() => goTo(VIEW.CART)}>
                    Koszyk ({totalItems})
                </button>
                <button type="button" className={view === VIEW.SUMMARY ? 'current' : ''}
                        onClick={openSummary}>
                    Podsumowanie
                </button>
                <button type="button" className={view === VIEW.CONFIRMATION ? 'current' : ''}
                        disabled={!createdOrder}>
                    Potwierdzenie
                </button>
            </nav>

            {error && <p className="status error">{error}</p>}

            {(clients.length === 0 || furniture.length === 0) && (
                <p className="status empty">
                    Do uruchomienia przypadku użycia potrzebny jest przynajmniej jeden klient i jeden mebel.
                </p>
            )}

            {view === VIEW.CATALOG && (
                <CatalogView
                    cartTotal={cartTotal}
                    furniture={furniture}
                    onAddToCart={id => updateQuantity(id, Number(quantities[id] || 0) + 1)}
                    onOpenCart={() => goTo(VIEW.CART)}
                    selectedClient={selectedClient}
                    totalItems={totalItems}
                />
            )}

            {view === VIEW.CART && (
                <CartView
                    cart={cart}
                    cartTotal={cartTotal}
                    minimalOrderValue={minimalOrderValue}
                    onBackToCatalog={() => goTo(VIEW.CATALOG)}
                    onClearCart={clearCart}
                    onOpenSummary={openSummary}
                    onRemoveFromCart={id => updateQuantity(id, 0)}
                    onUpdateQuantity={updateQuantity}
                />
            )}

            {view === VIEW.SUMMARY && (
                <SummaryView
                    cart={cart}
                    cartTotal={cartTotal}
                    deliveryAddress={deliveryAddress}
                    onBackToCart={() => goTo(VIEW.CART)}
                    onCreateOrder={createOrder}
                    paymentDeadline={paymentDeadline}
                    selectedClient={selectedClient}
                    setDeliveryAddress={value => {
                        setDeliveryAddress(value);
                        setCreatedOrder(null);
                    }}
                    submitting={submitting}
                />
            )}

            {view === VIEW.CONFIRMATION && createdOrder && (
                <ConfirmationView
                    createdOrder={createdOrder}
                    onNewOrder={startNewOrder}
                    onPayOrder={payOrder}
                    paying={paying}
                />
            )}
        </section>
    );
}

function PageHeader({onBack}) {
    return (
        <header className="use-case-shell-header">
            <div>
                <p className="eyebrow">Przypadek użycia UC02</p>
                <h1>Stwórz zamówienie</h1>
            </div>
            <button type="button" className="secondary-action" onClick={onBack}>
                Wróć do panelu
            </button>
        </header>
    );
}

function CatalogView({cartTotal, furniture, onAddToCart, onOpenCart, selectedClient, totalItems}) {
    return (
        <div className="view-stack">
            <section className="use-case-window client-window">
                <div>
                    <p className="eyebrow">Wylosowany klient</p>
                    <h2>{clientName(selectedClient)}</h2>
                    {selectedClient && (
                        <p>{selectedClient.email} · {selectedClient.phoneNumber} · poziom {selectedClient.loyaltyLevel}</p>
                    )}
                </div>
                <div className="mini-cart">
                    <span>{totalItems} szt. w koszyku</span>
                    <strong>{currency.format(cartTotal)}</strong>
                    <button type="button" className="primary-action" onClick={onOpenCart}>
                        Przejdź do koszyka
                    </button>
                </div>
            </section>

            <section className="use-case-window">
                <div className="panel-title-row">
                    <div>
                        <p className="eyebrow">Katalog</p>
                        <h2>Dostępne meble</h2>
                    </div>
                    <span className="pill">{furniture.length} produktów</span>
                </div>

                <div className="catalog-grid">
                    {furniture.map(item => (
                        <article className="catalog-card" key={item.id}>
                            <div>
                                <h3>{item.name}</h3>
                                <p>{item.categoryName}</p>
                            </div>
                            <div className="catalog-card-footer">
                                <strong>{currency.format(Number(item.price))}</strong>
                                <button type="button" className="primary-action" onClick={() => onAddToCart(item.id)}>
                                    Dodaj
                                </button>
                            </div>
                        </article>
                    ))}
                </div>
            </section>
        </div>
    );
}

function CartView({
                      cart,
                      cartTotal,
                      minimalOrderValue,
                      onBackToCatalog,
                      onClearCart,
                      onOpenSummary,
                      onRemoveFromCart,
                      onUpdateQuantity,
                  }) {
    return (
        <section className="use-case-window narrow-window">
            <div className="panel-title-row">
                <div>
                    <p className="eyebrow">Koszyk</p>
                    <h2>{cart.length === 0 ? 'Koszyk jest pusty' : `${cart.length} pozycji`}</h2>
                </div>
                <button type="button" className="secondary-action" onClick={onBackToCatalog}>
                    Kontynuuj zakupy
                </button>
            </div>

            {cart.length === 0 ? (
                <p className="empty-cart">Dodaj produkty z katalogu.</p>
            ) : (
                <div className="cart-list">
                    {cart.map(item => (
                        <div className="cart-row wide-cart-row" key={item.furnitureId}>
                            <div>
                                <strong>{item.furniture.name}</strong>
                                <span>{item.furniture.categoryName}</span>
                            </div>
                            <div>
                                <span>Cena</span>
                                <strong>{currency.format(Number(item.furniture.price))}</strong>
                            </div>
                            <div className="quantity-control">
                                <button type="button" onClick={() => onUpdateQuantity(item.furnitureId, item.quantity - 1)}>
                                    -
                                </button>
                                <input
                                    type="number"
                                    min="1"
                                    value={item.quantity}
                                    onChange={event => onUpdateQuantity(item.furnitureId, event.target.value)}
                                    aria-label={`Ilość: ${item.furniture.name}`}
                                />
                                <button type="button" onClick={() => onUpdateQuantity(item.furnitureId, item.quantity + 1)}>
                                    +
                                </button>
                            </div>
                            <button type="button" className="text-action" onClick={() => onRemoveFromCart(item.furnitureId)}>
                                Usuń
                            </button>
                        </div>
                    ))}
                </div>
            )}

            <div className="cart-footer">
                <button type="button" className="secondary-action" onClick={onClearCart} disabled={cart.length === 0}>
                    Wyczyść koszyk
                </button>
                <div className="cart-total">
                    <span>Suma</span>
                    <strong>{currency.format(cartTotal)}</strong>
                </div>
                <button type="button" className="primary-action" onClick={onOpenSummary} disabled={cart.length === 0}>
                    Przejdź do podsumowania
                </button>
            </div>

            {cartTotal > 0 && cartTotal < minimalOrderValue && (
                <p className="cart-warning">
                    Brakuje {currency.format(minimalOrderValue - cartTotal)} do minimalnej wartości zamówienia.
                </p>
            )}
        </section>
    );
}

function SummaryView({
                         cart,
                         cartTotal,
                         deliveryAddress,
                         onBackToCart,
                         onCreateOrder,
                         paymentDeadline,
                         selectedClient,
                         setDeliveryAddress,
                         submitting,
                     }) {
    return (
        <div className="view-stack">
            <section className="use-case-window narrow-window">
                <div className="panel-title-row">
                    <div>
                        <p className="eyebrow">Podsumowanie</p>
                        <h2>Dane zamówienia</h2>
                    </div>
                    <button type="button" className="secondary-action" onClick={onBackToCart}>
                        Wróć do koszyka
                    </button>
                </div>

                <div className="summary-grid">
                    <div className="summary-box">
                        <p className="eyebrow">Klient</p>
                        <h3>{clientName(selectedClient)}</h3>
                        {selectedClient && <p>{selectedClient.email}</p>}
                    </div>
                    <label className="summary-box form-row">
                        <span>Adres dostawy</span>
                        <input value={deliveryAddress} onChange={event => setDeliveryAddress(event.target.value)}/>
                    </label>
                    <div className="summary-box form-row">
                        <p className="eyebrow">Termin opłaty</p>
                        <h3>{paymentDeadline}</h3>
                    </div>
                </div>
            </section>

            <section className="use-case-window narrow-window">
                <div className="panel-title-row">
                    <div>
                        <p className="eyebrow">Pozycje</p>
                        <h2>Wybrane meble</h2>
                    </div>
                    <strong>{currency.format(cartTotal)}</strong>
                </div>

                <div className="summary-lines">
                    {cart.map(item => (
                        <div className="summary-line" key={item.furnitureId}>
                            <span>{item.furniture.name}</span>
                            <span>{item.quantity} x {currency.format(Number(item.furniture.price))}</span>
                            <strong>{currency.format(Number(item.furniture.price) * item.quantity)}</strong>
                        </div>
                    ))}
                </div>

                <div className="summary-actions">
                    <button type="button" className="primary-action" onClick={onCreateOrder} disabled={submitting}>
                        {submitting ? 'Tworzenie...' : 'Potwierdź zamówienie'}
                    </button>
                </div>
            </section>
        </div>
    );
}

function ConfirmationView({createdOrder, onNewOrder, onPayOrder, paying}) {
    return (
        <section className="use-case-window narrow-window confirmation-window">
            <div>
                <p className="eyebrow">Potwierdzenie</p>
                <h2>Zamówienie #{createdOrder.id}</h2>
                <p>Status: {statusLabels[createdOrder.status] || createdOrder.status}</p>
                <p>Termin opłaty: {createdOrder.paymentDeadline}</p>
                <p>Wartość: {currency.format(orderTotal(createdOrder))}</p>
            </div>

            <div className="summary-lines">
                {(createdOrder.lines || []).map(line => (
                    <div className="summary-line" key={line.id}>
                        <span>{line.furnitureName}</span>
                        <span>{line.quantity} x {currency.format(Number(line.unitPrice))}</span>
                        <strong>{currency.format(Number(line.unitPrice) * line.quantity)}</strong>
                    </div>
                ))}
            </div>

            <div className="result-actions">
                {createdOrder.status === 'CREATED' && (
                    <button type="button" className="primary-action" onClick={onPayOrder} disabled={paying}>
                        {paying ? 'Opłacanie...' : 'Opłać teraz'}
                    </button>
                )}
                <button type="button" className="secondary-action" onClick={onNewOrder}>
                    Nowe zamówienie
                </button>
            </div>
        </section>
    );
}

export default OrderUseCase;
