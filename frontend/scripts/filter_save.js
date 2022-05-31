/** @format */

export default function (serverRpc) {
  let template = `
        <div class="col-md-2">
            <div class="row">
                <div class="col-md-12">
                    <div class="form-group">
                        <label>Filter List</label>
                        <select class="filter_select selectpicker list-filter-select" id="filter_select" data-size="auto"
                                data-style="btn-white" data-live-search="true">
                            <option value="" data-filterid="">- Select Filter -</option>
                            <option value="-1" data-filterid="" @click="$emit('saveFilter')">Save as New Filter</option>
                        </select>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="form-group">
                        <button class="remove-filter-btn btn btn-warning" style="width: 100%; margin-bottom: 5px" @click="$emit('removeFilter')">Remove filter</button>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="form-group">
                        <button class="load-filter-btn btn btn-primary" style="width: 100%; margin-bottom: 5px" @click="$emit('loadFilter')">Load filter</button>
                    </div>
                </div>
            </div>
        </div>
    `;
  return {
    name: "filter-save",
    template: template,
  };
}
