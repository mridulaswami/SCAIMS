ALTER TABLE public.assets
    ADD CONSTRAINT assets_parent_asset_id_fkey
        FOREIGN KEY (parent_asset_id)
            REFERENCES public.assets(id);