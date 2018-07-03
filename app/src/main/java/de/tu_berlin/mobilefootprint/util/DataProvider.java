package de.tu_berlin.mobilefootprint.util;


import android.content.Context;
import android.util.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.tu_berlin.snet.cellservice.model.database.GeoDatabaseHelper;
import de.tu_berlin.snet.cellservice.model.record.AbstractCallOrText;
import de.tu_berlin.snet.cellservice.model.record.AbstractCellChange;
import de.tu_berlin.snet.cellservice.model.record.Call;
import de.tu_berlin.snet.cellservice.model.record.CellInfo;
import de.tu_berlin.snet.cellservice.model.record.Data;
import de.tu_berlin.snet.cellservice.model.record.Position;
import de.tu_berlin.snet.cellservice.model.record.TextMessage;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * DataProvider singleton will use {@link GeoDatabaseHelper} to query spatial database and request Mozilla Position
 * Service via RetroFit and {@link MozillaLocationService} to obtain GPS location estimation from the cell tower information
 *
 * @author johannes
 */
public class DataProvider {
    private static final Logger logger = LoggerManager.getLogger(DataProvider.class);
    public static final String URL_MLS = "https://location.services.mozilla.com";

    private static DataProvider instance;
    private GeoDatabaseHelper geoDatabaseHelper;
    private Map<Data, Position> dataLocationMap;
    private Map<Integer, Position> cellLocationMap;
    private Map<AbstractCallOrText, Position> callTextLocationMap;
    private Map<AbstractCellChange, Pair<Position, Position>> cellChangeLocationMap;
    private MozillaLocationService mls;
    private Context context;

    public static synchronized DataProvider getInstance(Context context) {
        if (instance == null) {
            instance = new DataProvider(context);
        }
        return instance;
    }

    private DataProvider(Context context) {
        this.context = context;
        dataLocationMap = new HashMap<>();
        cellLocationMap = new HashMap<>();
        callTextLocationMap = new HashMap<>();
        cellChangeLocationMap = new HashMap<>();
        geoDatabaseHelper = GeoDatabaseHelper.getInstance(context);
        setUpRetrofit();
    }

    private void setUpRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_MLS)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mls = retrofit.create(MozillaLocationService.class);
    }


    public void loadData() {
        processCellLocations();
        processDataPositions();
        processCallAndTextLocations();
        processCellChangeLocations();
    }


    public void processDataPositions() {
        for (Data d : geoDatabaseHelper.getAllDataRecords()) {
            Position position = retrievePosition(d.getCell());
            if (position != null) {
                position.addTime(d.getSessionStart());
                position.addTime(d.getSessionEnd());
                dataLocationMap.put(d, position);
            }
        }

    }

    public void processCellLocations() {
        for (CellInfo c : geoDatabaseHelper.getAllCellRecords()) {
            Position position = getMLSLocation(c);
            if (position != null) {
                cellLocationMap.put(c.getId(), position);
            }
        }
    }

    public void processCallAndTextLocations() {
        ArrayList<AbstractCallOrText> callTextRecords = new ArrayList<>();
        callTextRecords.addAll(geoDatabaseHelper.getAllCallRecords());
        callTextRecords.addAll(geoDatabaseHelper.getAllTextMessageRecords());
        for (AbstractCallOrText ct : callTextRecords) {
            Position position;
            if (ct instanceof Call) {
                //todo handle handovers
                position = retrievePosition(((Call) ct).getStartCell());
                position.addTime(((Call) ct).getStartTime());
                position.addTime(((Call) ct).getEndTime());
            } else {
                position = retrievePosition(((TextMessage) ct).getCell());
                position.addTime(((TextMessage) ct).getTime());
            }
            if (position != null) {
                callTextLocationMap.put(ct, position);
            }
        }
    }

    public void processCellChangeLocations() {
        ArrayList<AbstractCellChange> cellChangeRecords = new ArrayList<>();
        cellChangeRecords.addAll(geoDatabaseHelper.getAllLocationUpdateRecords());
        cellChangeRecords.addAll(geoDatabaseHelper.getAllHandoverRecords());
        for (AbstractCellChange cc : cellChangeRecords) {
            Position startPosition = retrievePosition(cc.getStartCell());
            Position endPosition = retrievePosition(cc.getEndCell());
            if (startPosition != null && endPosition != null) {
                startPosition.addTime(cc.getTimestamp());
                endPosition.addTime(cc.getTimestamp());
                cellChangeLocationMap.put(cc, new Pair(startPosition, endPosition));
            }
        }
    }

    private Position retrievePosition(CellInfo cellInfo) {
        if (getCellLocationMap().containsKey(cellInfo.getId())) {
            return getCellLocationMap().get(cellInfo.getId());
        } else {
            return geoDatabaseHelper.getPositionsFromId(cellInfo.getId());
        }
    }


    private JsonObject jsonizeCellInfo(CellInfo cellinfo) {
        JsonObject celldata = new JsonObject();
        JsonObject celltowers = new JsonObject();
        JsonArray tower = new JsonArray();
        tower.add(celldata);
        // this is set to a constant value and required by MLS
        celldata.addProperty("radioType", "wcdma");
        celldata.addProperty("mobileCountryCode", cellinfo.getMcc());
        celldata.addProperty("mobileNetworkCode", cellinfo.getMnc());
        celldata.addProperty("locationAreaCode", cellinfo.getLac());
        celldata.addProperty("cellId", cellinfo.getCellId());
        // this is set to a constant value and required by MLS
        celldata.addProperty("signalStrength", -60);
        celltowers.add("cellTowers", tower);
        return celltowers;
    }

    private Position getMLSLocation(CellInfo cellInfo) {
        Position position = geoDatabaseHelper.getPositionsFromId(cellInfo.getId());
        if (position != null) {
            return position;
        } else {
            retrofit2.Call mlsCall = mls.getLocation(jsonizeCellInfo(cellInfo));
            try {
                retrofit2.Response j = mlsCall.execute();
                JsonObject response = (JsonObject) j.body();
                JsonObject location = response.getAsJsonObject("location");
                position = new Position(location.get("lat").getAsDouble(), location.get("lng").getAsDouble(), response.get("accuracy").getAsDouble(), cellInfo.getId());
                geoDatabaseHelper.insertRecord(position);
            } catch (IOException e) {
                logger.e("Error during json processing", e);
            }
            return position;
        }
    }

    public Map<AbstractCallOrText, Position> getCallTextLocationMap() {
        return callTextLocationMap;
    }

    public Map<AbstractCellChange, Pair<Position, Position>> getCellChangeLocationMap() {
        return cellChangeLocationMap;
    }

    public Map<Integer, Position> getCellLocationMap() {
        return cellLocationMap;
    }

    public Map<Data, Position> getDataLocationMap() {
        return dataLocationMap;
    }
}
