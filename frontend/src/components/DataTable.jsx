import { useEffect, useState } from 'react';
import { apiRequest } from '../services/api';

const statusLabels = {
    CREATED: 'Utworzone',
    PAID: 'Opłacone',
    COMPLETED: 'Zrealizowane',
    CANCELLED: 'Anulowane',
};

function formatValue(value, column) {
    if (column === 'orderIds' && Array.isArray(value)) return String(value.length);
    if (Array.isArray(value)) return `[Lista: ${value.length} elem.]`;
    if (value && typeof value === 'object') return value.name || value.id || '[Obiekt]';
    if (typeof value === 'boolean') return value ? 'Tak' : 'Nie';
    return String(value ?? '');
}

function clientLabel(client) {
    if (client.companyName) return client.companyName;
    return [client.firstName, client.lastName].filter(Boolean).join(' ') || `Klient #${client.id}`;
}

function listTitle(row, lists) {
    if (lists.some(([name]) => name === 'orders' || name === 'orderIds')) {
        return `Zamówienia: ${clientLabel(row)}`;
    }

    return 'Elementy listy';
}

function rowLists(row) {
    return Object.entries(row).filter(([, value]) => Array.isArray(value));
}

function renderListItem(name, item, index) {
    if (name === 'orders' && item && typeof item === 'object') {
        const lines = item.lines || [];

        return (
            <li className="order-list-item" key={item.id || index}>
                <div>
                    <strong>Zamówienie #{item.id}</strong>
                    <span>{statusLabels[item.status] || item.status}</span>
                </div>
                <div className="order-meta">
                    <span>Utworzone: {formatValue(item.createdOn)}</span>
                    <span>Termin opłaty: {formatValue(item.paymentDeadline)}</span>
                    <span>Dostawa: {formatValue(item.deliveryDeadline)}</span>
                </div>
                <p>{item.deliveryAddress}</p>
                <div className="order-lines">
                    <strong>Zamówione elementy</strong>
                    {lines.length === 0 ? (
                        <span>Brak pozycji.</span>
                    ) : (
                        <ul>
                            {lines.map(line => (
                                <li key={line.id}>
                                    <span>{line.furnitureName}</span>
                                    <span>{line.quantity} x {formatValue(line.unitPrice)}</span>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </li>
        );
    }

    return <li key={item?.id || index}>{formatValue(item)}</li>;
}

function DataTable({ title, endpoint }) {
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [selectedRow, setSelectedRow] = useState(null);
    const [modalLoading, setModalLoading] = useState(false);
    const [modalError, setModalError] = useState(null);

    useEffect(() => {
        let active = true;

        apiRequest(endpoint)
            .then(result => {
                if (!active) return;
                setData(result);
                setError(null);
            })
            .catch(err => {
                if (!active) return;
                setError(err.message);
            });

        return () => {
            active = false;
        };
    }, [endpoint]);

    if (error) return <p className="status error">Błąd: {error}</p>;
    if (!data) return <p className="status loading">Ładowanie...</p>;
    if (data.length === 0) return <p className="status empty">Brak danych.</p>;

    const columns = Object.keys(data[0]);

    return (
        <div className="table-container">
            <h2>{title}</h2>
            <table>
                <thead>
                <tr>
                    {columns.map(column => <th key={column}>{column}</th>)}
                </tr>
                </thead>
                <tbody>
                {data.map((row, index) => {
                    const lists = rowLists(row);
                    const clickable = lists.length > 0;

                    async function openLists() {
                        if (!clickable) return;

                        setSelectedRow(row);
                        setModalError(null);

                        if (!row.orderIds) return;

                        setModalLoading(true);
                        try {
                            const orders = await Promise.all(
                                row.orderIds.map(orderId => apiRequest(`/api/orders/${orderId}`))
                            );
                            setSelectedRow({...row, orders});
                        } catch (err) {
                            setModalError(err.message || 'Nie udało się pobrać zamówień.');
                        } finally {
                            setModalLoading(false);
                        }
                    }

                    return (
                        <tr
                            key={row.id || index}
                            className={clickable ? 'clickable-row' : ''}
                            tabIndex={clickable ? 0 : undefined}
                            onClick={openLists}
                            onKeyDown={event => {
                                if (event.key === 'Enter' || event.key === ' ') openLists();
                            }}
                        >
                            {columns.map(column => (
                                <td key={column}>{formatValue(row[column], column)}</td>
                            ))}
                        </tr>
                    );
                })}
                </tbody>
            </table>

            {selectedRow && (
                <div className="modal-backdrop" onClick={() => setSelectedRow(null)}>
                    <section className="modal" onClick={event => event.stopPropagation()}>
                        <div className="modal-header">
                            <h3>{listTitle(selectedRow, rowLists(selectedRow))}</h3>
                            <button type="button" onClick={() => setSelectedRow(null)}>Zamknij</button>
                        </div>

                        {modalLoading && <p className="status loading">Ładowanie zamówień...</p>}
                        {modalError && <p className="status error">{modalError}</p>}

                        {rowLists(selectedRow)
                            .filter(([name]) => name !== 'orderIds' || !selectedRow.orders)
                            .map(([name, items]) => (
                            <div className="list-preview" key={name}>
                                <h4>{name === 'orderIds' ? 'orders' : name}</h4>
                                {items.length === 0 ? (
                                    <p>Lista jest pusta.</p>
                                ) : (
                                    <ul className={name === 'orders' ? 'object-list' : ''}>
                                        {items.map((item, index) => (
                                            renderListItem(name, item, index)
                                        ))}
                                    </ul>
                                )}
                            </div>
                        ))}
                    </section>
                </div>
            )}
        </div>
    );
}

export default DataTable;
