/*
Copyright (c) 2024 Volkswagen AG
Copyright (c) 2024 Fraunhofer-Gesellschaft zur Foerderung der angewandten Forschung e.V. (represented by Fraunhofer ISST)
Copyright (c) 2024 Contributors to the Eclipse Foundation

See the NOTICE file(s) distributed with this work for additional
information regarding copyright ownership.

This program and the accompanying materials are made available under the
terms of the Apache License, Version 2.0 which is available at
https://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations
under the License.

SPDX-License-Identifier: Apache-2.0
*/

import { Input } from '@catena-x/portal-shared-components';
import { useMaterials } from '@features/stock-view/hooks/useMaterials';
import { usePartners } from '@features/stock-view/hooks/usePartners';
import { useSites } from '@features/stock-view/hooks/useSites';
import { MaterialDescriptor } from '@models/types/data/material-descriptor';
import { Site } from '@models/types/edc/site';
import { Autocomplete, capitalize, Grid, InputLabel } from '@mui/material';
import { getPartnerType } from '../util/helpers';
import { LabelledAutoComplete } from '@components/ui/LabelledAutoComplete';

type DashboardFiltersProps = {
    type: 'customer' | 'supplier';
    material: MaterialDescriptor | null;
    site: Site | null;
    partnerSites: Site[] | null;
    onMaterialChange: (material: MaterialDescriptor | null) => void;
    onSiteChange: (site: Site | null) => void;
    onPartnerSitesChange: (sites: Site[] | null) => void;
};

export const DashboardFilters = ({
    type,
    material,
    site,
    partnerSites,
    onMaterialChange,
    onSiteChange,
    onPartnerSitesChange,
}: DashboardFiltersProps) => {
    const { materials } = useMaterials(type === 'customer' ? 'material' : 'product');
    const { partners } = usePartners(type === 'customer' ? 'material' : 'product', material?.ownMaterialNumber ?? null);
    const { sites } = useSites();
    return (
        <Grid container spacing={2} maxWidth={1024} sx={{p: 2}}>
            <Grid item md={6} paddingTop='0 !important'>
                <LabelledAutoComplete
                    id="material"
                    value={material}
                    options={materials ?? []}
                    getOptionLabel={(option) => `${option.description} (${option.ownMaterialNumber})`}
                    label="Material*" 
                    placeholder="Select a Material"
                    onChange={(_, newValue) => onMaterialChange(newValue || null)}
                />
            </Grid>
            <Grid item md={6} paddingTop='0 !important'>
                <LabelledAutoComplete
                    id="site"
                    value={site}
                    options={sites ?? []}
                    getOptionLabel={(option) => `${option.name} (${option.bpns})`}
                    disabled={!material}
                    label="Site*"
                    placeholder="Select a Site"
                    onChange={(_, newValue) => onSiteChange(newValue || null)}
                />
            </Grid>
            <Grid item md={12} paddingTop='0 !important'>
                <InputLabel>{capitalize(getPartnerType(type))} Sites*</InputLabel>
                <Autocomplete
                    id="partner-site"
                    value={partnerSites ?? []}
                    options={partners?.reduce((acc: Site[], p) => {
                        const sitesWithBpnl = p.sites.map(site => ({
                            ...site,
                            belongsToPartnerBpnl: p.bpnl
                        }));
                        return [...acc, ...sitesWithBpnl];
                    }, []) ?? [] }
                    disabled={!site}
                    getOptionLabel={(option) => `${option.name} (${option.bpns})`}
                    isOptionEqualToValue={(option, value) => option.bpns === value.bpns}
                    renderInput={(params) => (
                        <Input
                            {...params}
                            hiddenLabel
                            placeholder={`Select a ${capitalize(getPartnerType(type))} site`}
                        />
                    )}
                    onChange={(_, newValue) => onPartnerSitesChange(newValue?.length > 0 ? newValue : null)}
                    multiple={true}
                />
            </Grid>
        </Grid>
    );
};
