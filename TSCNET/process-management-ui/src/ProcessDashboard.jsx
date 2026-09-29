import React, { useState } from 'react';
import ProcessLogsView from './ProcessLogsView';
import ProcurementOffersView from './ProcurementOffersView';

export default function ProcessDashboard() {
    const [activeTab, setActiveTab] = useState('logs'); // 'logs' | 'data'

    return (
        <div className="p-6 max-w-7xl mx-auto space-y-6 font-sans">
            {/* Main Header & Navigation Tabs */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b pb-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-800">
                        Daily Process Management
                    </h1>
                    <p className="text-xs text-gray-500 mt-1">
                        Monitor execution logs, trigger process runs, and inspect procurement assessments and offers
                    </p>
                </div>

                <div className="inline-flex rounded-md shadow-sm border border-gray-300 p-1 bg-gray-50">
                    <button
                        type="button"
                        onClick={() => setActiveTab('logs')}
                        className={`px-4 py-2 text-sm font-medium rounded-md transition ${
                            activeTab === 'logs'
                                ? 'bg-blue-600 text-white shadow-sm'
                                : 'text-gray-600 hover:text-gray-900'
                        }`}
                    >
                        Screen 1: Process Execution Logs
                    </button>
                    <button
                        type="button"
                        onClick={() => setActiveTab('data')}
                        className={`px-4 py-2 text-sm font-medium rounded-md transition ${
                            activeTab === 'data'
                                ? 'bg-blue-600 text-white shadow-sm'
                                : 'text-gray-600 hover:text-gray-900'
                        }`}
                    >
                        Screen 2: Procurements & Offers
                    </button>
                </div>
            </div>

            {/* Screen Content */}
            {activeTab === 'logs' ? <ProcessLogsView /> : <ProcurementOffersView />}
        </div>
    );
}