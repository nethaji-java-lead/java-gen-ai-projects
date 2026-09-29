import React, { useState, useEffect } from 'react';

export default function ProcurementOffersView() {
    const [businessDate, setBusinessDate] = useState(
        new Date().toISOString().split('T')[0]
    );
    const [subTab, setSubTab] = useState('assessments'); // 'assessments' | 'offers'
    const [assessments, setAssessments] = useState([]);
    const [offers, setOffers] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        fetchData();
    }, [businessDate]);

    const fetchData = async () => {
        setLoading(true);
        try {
            const [procurementRes, offersRes] = await Promise.all([
                fetch(`/api/process/procurement?businessDate=${businessDate}`),
                fetch(`/api/process/offers?businessDate=${businessDate}`)
            ]);

            if (procurementRes.ok) {
                const procurementData = await procurementRes.json();
                setAssessments(procurementData);
            }
            if (offersRes.ok) {
                const offersData = await offersRes.json();
                setOffers(offersData);
            }
        } catch (err) {
            console.error('Error fetching procurement data:', err);
        } finally {
            setLoading(false);
        }
    };

    const getStatusBadge = (status) => {
        const styles = {
            ACCEPT: 'bg-green-100 text-green-800 border-green-300',
            REVIEW: 'bg-amber-100 text-amber-800 border-amber-300',
            REJECT: 'bg-red-100 text-red-800 border-red-300',
        };
        return (
            <span className={`px-2.5 py-0.5 text-xs font-semibold border rounded-full inline-block ${styles[status] || 'bg-gray-100 text-gray-800'}`}>
                {status}
            </span>
        );
    };

    return (
        <div className="space-y-6">
            {/* Filter Header */}
            <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 flex flex-wrap items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                    <label className="text-sm font-medium text-gray-700">Business Date:</label>
                    <input
                        type="date"
                        value={businessDate}
                        onChange={(e) => setBusinessDate(e.target.value)}
                        className="border rounded px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
                <button
                    onClick={fetchData}
                    className="bg-gray-100 hover:bg-gray-200 text-gray-700 border px-3 py-1.5 rounded text-sm transition"
                >
                    {loading ? 'Loading...' : 'Refresh Data'}
                </button>
            </div>

            {/* Content Display Card */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-5 space-y-4">
                {/* Sub Tab Switcher */}
                <div className="flex border-b">
                    <button
                        onClick={() => setSubTab('assessments')}
                        className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
                            subTab === 'assessments'
                                ? 'border-blue-600 text-blue-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700'
                        }`}
                    >
                        Procurement Assessments ({assessments.length})
                    </button>
                    <button
                        onClick={() => setSubTab('offers')}
                        className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
                            subTab === 'offers'
                                ? 'border-blue-600 text-blue-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700'
                        }`}
                    >
                        Procurement Offers ({offers.length})
                    </button>
                </div>

                {/* Assessments View */}
                {subTab === 'assessments' && (
                    <div className="overflow-x-auto border rounded-lg">
                        <table className="w-full text-left text-sm border-collapse">
                            <thead>
                            <tr className="bg-gray-100 border-b text-xs text-gray-600 uppercase font-semibold">
                                <th className="p-3">Business Date</th>
                                <th className="p-3">Status</th>
                                <th className="p-3">Total Quantity</th>
                                <th className="p-3">Weighted Avg Price</th>
                                <th className="p-3">Complete</th>
                                <th className="p-3">Quantity Threshold</th>
                                <th className="p-3">Price Threshold</th>
                                <th className="p-3">Reason</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-200">
                            {assessments.length > 0 ? (
                                assessments.map((item, index) => (
                                    <tr key={index} className="hover:bg-gray-50">
                                        <td className="p-3 font-mono text-xs">{item.businessDate}</td>
                                        <td className="p-3">{getStatusBadge(item.status)}</td>
                                        <td className="p-3 font-bold text-gray-800">{item.totalQuantity}</td>
                                        <td className="p-3 font-bold text-gray-800">{item.weightedAveragePrice}</td>
                                        <td className="p-3 text-xs">{item.complete ? 'Yes' : 'No'}</td>
                                        <td className="p-3 text-xs">{item.quantityThresholdMet ? 'Met' : 'Failed'}</td>
                                        <td className="p-3 text-xs">{item.priceThresholdMet ? 'Met' : 'Failed'}</td>
                                        <td className="p-3 text-xs text-gray-600">{item.reason}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="8" className="p-4 text-center text-gray-500 text-sm">
                                        No procurement assessment records found for date <span className="font-semibold">{businessDate}</span>.
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )}

                {/* Offers View */}
                {subTab === 'offers' && (
                    <div className="overflow-x-auto border rounded-lg">
                        <table className="w-full text-left text-sm border-collapse">
                            <thead>
                            <tr className="bg-gray-100 border-b text-xs text-gray-600 uppercase font-semibold">
                                <th className="p-3">mRID</th>
                                <th className="p-3">Business Type</th>
                                <th className="p-3">Market Agreement</th>
                                <th className="p-3">Product Type</th>
                                <th className="p-3">PSR Type</th>
                                <th className="p-3">Flow Dir.</th>
                                <th className="p-3">Quantity</th>
                                <th className="p-3">Price</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-200">
                            {offers.length > 0 ? (
                                offers.map((offer, index) => (
                                    <tr key={index} className="hover:bg-gray-50">
                                        <td className="p-3 font-mono text-xs text-gray-800 font-semibold">{offer.mrid}</td>
                                        <td className="p-3 text-xs">{offer.businessType}</td>
                                        <td className="p-3 text-xs">{offer.marketAgreementType}</td>
                                        <td className="p-3 text-xs">{offer.originalMarketProductType}</td>
                                        <td className="p-3 text-xs">{offer.psrType}</td>
                                        <td className="p-3 text-xs">{offer.flowDirection}</td>
                                        <td className="p-3 font-medium text-gray-800">{offer.quantity} {offer.quantityMeasureUnit}</td>
                                        <td className="p-3 font-medium text-gray-800">{offer.procurementPrice} {offer.currencyUnit}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="8" className="p-4 text-center text-gray-500 text-sm">
                                        No procurement offer records found for date <span className="font-semibold">{businessDate}</span>.
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}