import React, { useState, useEffect } from 'react';

const API_BASE_URL = '/api/process';

export default function ProcessLogsView() {
    const [selectedDate, setSelectedDate] = useState(
        new Date().toISOString().split('T')[0]
    );
    const [fileFilter, setFileFilter] = useState('ALL'); // ALL | SUCCESS | FAILED

    const [latestProcess, setLatestProcess] = useState(null);
    const [failedProcesses, setFailedProcesses] = useState([]);
    const [expandedLogId, setExpandedLogId] = useState(null);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ text: '', type: '' });

    useEffect(() => {
        fetchLatestProcess();
        fetchFailedProcesses();
    }, [selectedDate]);

    const showNotification = (text, type = 'info') => {
        setMessage({ text, type });
        setTimeout(() => setMessage({ text: '', type: '' }), 6000);
    };

    const triggerProcess = async (dateToTrigger = selectedDate) => {
        setLoading(true);
        try {
            const response = await fetch(
                `${API_BASE_URL}/trigger?businessDate=${dateToTrigger}`,
                { method: 'POST' }
            );

            // Read Content-Type header directly from response
            const contentType = response.headers.get("content-type");

            // Handle plain text response (e.g. NoXmlFileException)
            if (contentType && contentType.includes("text/plain")) {
                const textMessage = await response.text();
                showNotification(textMessage, 'info');

                fetchLatestProcess();
                fetchFailedProcesses();
                return;
            }

            // Handle standard JSON response
            const data = await response.json();
            showNotification(
                `Executed successfully with status: ${data.status}`,
                'success'
            );

            fetchLatestProcess();
            fetchFailedProcesses();
        } catch (err) {
            showNotification(`Execution Failed: ${err.message}`, 'error');
            // Refresh logs to show the recorded FAILED status if persisted
            fetchLatestProcess();
            fetchFailedProcesses();
        } finally {
            setLoading(false);
        }
    };

    const fetchFailedProcesses = async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/failed`);
            if (response.ok) {
                const data = await response.json();
                setFailedProcesses(data);
            }
        } catch (err) {
            console.error('Error fetching failed processes:', err);
        }
    };

    const fetchLatestProcess = async () => {
        try {
            if (!selectedDate) {
                console.error("BusinessDate is missing:", selectedDate);
                return;
            }
            const response = await fetch(
                `${API_BASE_URL}/latest?businessDate=${selectedDate}`
            );
            if (response.ok) {
                const data = await response.json();
                setLatestProcess(data);
            } else {
                setLatestProcess(null);
            }
        } catch (err) {
            console.error('Error fetching latest process:', err);
            setLatestProcess(null);
        }
    };

    const toggleExpandLog = (id) => {
        setExpandedLogId(expandedLogId === id ? null : id);
    };

    const getStatusBadge = (status) => {
        const styles = {
            SUCCESS: 'bg-green-100 text-green-800 border-green-300',
            FAILED: 'bg-red-100 text-red-800 border-red-300',
            PARTIAL_SUCCESS: 'bg-amber-100 text-amber-800 border-amber-300',
            IN_PROGRESS: 'bg-blue-100 text-blue-800 border-blue-300',
            ALREADY_EXECUTED: 'bg-gray-100 text-gray-800 border-gray-300',
        };
        return (
            <span className={`px-2.5 py-0.5 text-xs font-semibold border rounded-full inline-block ${styles[status] || 'bg-gray-100 text-gray-800'}`}>
                {status}
            </span>
        );
    };

    const formatDuration = (start, end) => {
        if (!start || !end) return '-';
        const ms = new Date(end) - new Date(start);
        return ms < 1000 ? `${ms} ms` : `${(ms / 1000).toFixed(2)} s`;
    };

    const filteredFileLogs = latestProcess?.fileLogs?.filter((file) => {
        if (fileFilter === 'SUCCESS') return file.status === 'SUCCESS';
        if (fileFilter === 'FAILED') return file.status === 'FAILED';
        return true;
    }) || [];

    return (
        <div className="space-y-6">
            {/* Notification Banner */}
            {message.text && (
                <div className={`p-4 rounded-md text-sm font-medium transition-all ${
                    message.type === 'error'
                        ? 'bg-red-50 text-red-800 border border-red-300 shadow-sm'
                        : message.type === 'info'
                            ? 'bg-blue-50 text-blue-800 border border-blue-300 shadow-sm'
                            : 'bg-green-50 text-green-800 border border-green-300 shadow-sm'
                }`}>
                    <div className="flex items-center gap-2">
                        {message.type === 'error' ? (
                            <span className="font-bold text-red-600">⚠ Error:</span>
                        ) : message.type === 'info' ? (
                            <span className="font-bold text-blue-600">ℹ Info:</span>
                        ) : (
                            <span className="font-bold text-green-600">✓ Success:</span>
                        )}
                        <span>{message.text}</span>
                    </div>
                </div>
            )}

            {/* Action Bar */}
            <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 flex flex-wrap items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                    <label className="text-sm font-medium text-gray-700">Business Date:</label>
                    <input
                        type="date"
                        value={selectedDate}
                        onChange={(e) => setSelectedDate(e.target.value)}
                        className="border rounded px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
                <button
                    onClick={() => triggerProcess(selectedDate)}
                    disabled={loading}
                    className="bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white font-medium px-4 py-2 rounded text-sm transition shadow-sm flex items-center gap-2"
                >
                    {loading && (
                        <svg className="animate-spin h-4 w-4 text-white" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
                        </svg>
                    )}
                    {loading ? 'Executing...' : 'Trigger Process Execution'}
                </button>
            </div>

            {/* Latest Execution Log View */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-5 space-y-4">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b pb-3">
                    <h2 className="text-lg font-semibold text-gray-800">
                        Latest Process Execution Log for {selectedDate}
                    </h2>
                    {latestProcess && (
                        <div className="text-xs text-gray-500">
                            Execution Time: <span className="font-mono text-gray-700">{new Date(latestProcess.startTime).toLocaleTimeString()}</span> ({formatDuration(latestProcess.startTime, latestProcess.endTime)})
                        </div>
                    )}
                </div>

                {latestProcess ? (
                    <div>
                        {/* Summary Metrics Grid */}
                        <div className="grid grid-cols-2 md:grid-cols-7 gap-3 text-sm bg-gray-50 p-4 rounded-lg border mb-5">
                            <div>
                                <span className="text-gray-500 block text-xs uppercase font-medium">Exec ID</span>
                                <span className="font-mono font-bold text-gray-800">#{latestProcess.id}</span>
                            </div>
                            <div>
                                <span className="text-gray-500 block text-xs uppercase font-medium">Status</span>
                                {getStatusBadge(latestProcess.status)}
                            </div>
                            <div>
                                <span className="text-gray-500 block text-xs uppercase font-medium">Initiation</span>
                                <span className="font-medium text-gray-700">{latestProcess.initiationType}</span>
                            </div>
                            <div>
                                <span className="text-gray-500 block text-xs uppercase font-medium">Triggered By</span>
                                <span className="font-medium text-gray-700">{latestProcess.triggeredBy}</span>
                            </div>
                            <div>
                                <span className="text-gray-500 block text-xs uppercase font-medium">Total Files</span>
                                <span className="font-bold text-gray-800">{latestProcess.totalFilesCount}</span>
                            </div>
                            <div>
                                <span className="text-gray-500 block text-xs uppercase font-medium">Processed</span>
                                <span className="font-bold text-green-700">{latestProcess.processedFilesCount}</span>
                            </div>
                            <div>
                                <span className="text-gray-500 block text-xs uppercase font-medium">Failed</span>
                                <span className="font-bold text-red-600">{latestProcess.failedFilesCount}</span>
                            </div>
                        </div>

                        {/* File Logs Section */}
                        <div className="space-y-3">
                            <div className="flex items-center justify-between">
                                <h3 className="text-sm font-semibold text-gray-700">Associated File Logs:</h3>
                                <div className="flex gap-1 text-xs bg-gray-100 p-1 rounded-md">
                                    <button
                                        onClick={() => setFileFilter('ALL')}
                                        className={`px-2.5 py-1 rounded font-medium ${fileFilter === 'ALL' ? 'bg-white shadow-sm text-gray-800' : 'text-gray-600 hover:text-gray-900'}`}
                                    >
                                        All ({latestProcess.fileLogs?.length || 0})
                                    </button>
                                    <button
                                        onClick={() => setFileFilter('SUCCESS')}
                                        className={`px-2.5 py-1 rounded font-medium ${fileFilter === 'SUCCESS' ? 'bg-white shadow-sm text-green-800' : 'text-gray-600 hover:text-gray-900'}`}
                                    >
                                        Success ({latestProcess.processedFilesCount})
                                    </button>
                                    <button
                                        onClick={() => setFileFilter('FAILED')}
                                        className={`px-2.5 py-1 rounded font-medium ${fileFilter === 'FAILED' ? 'bg-white shadow-sm text-red-800' : 'text-gray-600 hover:text-gray-900'}`}
                                    >
                                        Failed ({latestProcess.failedFilesCount})
                                    </button>
                                </div>
                            </div>

                            <div className="overflow-x-auto border rounded-lg">
                                <table className="w-full text-left text-xs border-collapse">
                                    <thead>
                                    <tr className="bg-gray-100 text-gray-600 border-b uppercase font-semibold">
                                        <th className="p-3 w-20">File ID</th>
                                        <th className="p-3 w-56">File Name</th>
                                        <th className="p-3 w-32">Status</th>
                                        <th className="p-3">Error Message</th>
                                    </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-200">
                                    {filteredFileLogs.length > 0 ? (
                                        filteredFileLogs.map((file) => (
                                            <tr key={file.id} className="hover:bg-gray-50">
                                                <td className="p-3 font-mono text-gray-500">#{file.id}</td>
                                                <td className="p-3 font-medium text-gray-800 font-mono">{file.fileName}</td>
                                                <td className="p-3">{getStatusBadge(file.status)}</td>
                                                <td className="p-3">
                                                    {file.errorMessage ? (
                                                        <div className="bg-red-50 border border-red-200 text-red-800 p-2 rounded text-[11px] font-mono leading-relaxed max-h-24 overflow-y-auto">
                                                            {file.errorMessage}
                                                        </div>
                                                    ) : (
                                                        <span className="text-gray-400 italic">No errors</span>
                                                    )}
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan="4" className="p-4 text-center text-gray-500">
                                                No file logs found for filter: <span className="font-semibold">{fileFilter}</span>
                                            </td>
                                        </tr>
                                    )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className="p-8 text-center text-gray-500 bg-gray-50 rounded-lg border border-dashed">
                        No process execution recorded for business date <span className="font-semibold text-gray-700">{selectedDate}</span>.
                    </div>
                )}
            </div>

            {/* Failed Processes Section */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-5 space-y-4">
                <div className="flex justify-between items-center border-b pb-3">
                    <h2 className="text-lg font-semibold text-gray-800">
                        Failed Processes
                    </h2>
                    <button
                        onClick={fetchFailedProcesses}
                        className="text-xs text-blue-600 hover:text-blue-800 font-medium hover:underline"
                    >
                        Refresh List
                    </button>
                </div>

                <div className="overflow-x-auto border rounded-lg">
                    <table className="w-full text-left text-sm border-collapse">
                        <thead>
                        <tr className="bg-gray-100 border-b text-xs text-gray-600 uppercase font-semibold">
                            <th className="p-3">Exec ID</th>
                            <th className="p-3">Initiation</th>
                            <th className="p-3">Status</th>
                            <th className="p-3">Start Time</th>
                            <th className="p-3">Failed Files</th>
                            <th className="p-3">Actions</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                        {failedProcesses.length > 0 ? (
                            failedProcesses.map((p) => (
                                <React.Fragment key={p.id}>
                                    <tr className="hover:bg-gray-50">
                                        <td className="p-3 font-mono font-semibold">#{p.id}</td>
                                        <td className="p-3 text-xs">{p.initiationType}</td>
                                        <td className="p-3">{getStatusBadge(p.status)}</td>
                                        <td className="p-3 text-xs text-gray-600">{new Date(p.startTime).toLocaleString()}</td>
                                        <td className="p-3 font-bold text-red-600">{p.failedFilesCount}</td>
                                        <td className="p-3 space-x-2">
                                            <button
                                                onClick={() => toggleExpandLog(p.id)}
                                                className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 border px-2.5 py-1 rounded transition"
                                            >
                                                {expandedLogId === p.id ? 'Hide Details' : 'View Errors'}
                                            </button>

                                            <button
                                                onClick={() => triggerProcess(p.startTime ? p.startTime.split('T')[0] : selectedDate)}
                                                disabled={loading}
                                                className="text-xs bg-red-600 hover:bg-red-700 text-white px-2.5 py-1 rounded transition disabled:bg-red-300 shadow-sm"
                                            >
                                                Retry
                                            </button>
                                        </td>
                                    </tr>

                                    {expandedLogId === p.id && (
                                        <tr className="bg-red-50/40">
                                            <td colSpan="6" className="p-4 border-b">
                                                <div className="text-xs space-y-2">
                                                    <span className="font-semibold text-gray-800 block">Failed Files Breakdown:</span>
                                                    {p.fileLogs && p.fileLogs.filter(f => f.status === 'FAILED').length > 0 ? (
                                                        <ul className="space-y-2">
                                                            {p.fileLogs.filter(f => f.status === 'FAILED').map((file) => (
                                                                <li key={file.id} className="bg-white p-2.5 rounded border border-red-200">
                                                                    <div className="font-mono font-semibold text-gray-800">{file.fileName}</div>
                                                                    <div className="text-red-700 font-mono text-[11px] mt-1">{file.errorMessage}</div>
                                                                </li>
                                                            ))}
                                                        </ul>
                                                    ) : (
                                                        <div className="text-gray-500 italic">No specific file errors recorded.</div>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    )}
                                </React.Fragment>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="6" className="p-4 text-center text-gray-500 text-sm">
                                    No failed processes found.
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}